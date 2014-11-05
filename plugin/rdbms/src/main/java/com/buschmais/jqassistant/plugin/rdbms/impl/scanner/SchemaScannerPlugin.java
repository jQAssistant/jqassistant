package com.buschmais.jqassistant.plugin.rdbms.impl.scanner;

import static com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import schemacrawler.schema.*;
import schemacrawler.schemacrawler.SchemaCrawlerException;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaInfoLevel;
import schemacrawler.tools.options.InfoLevel;
import schemacrawler.utility.SchemaCrawlerUtility;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.java.api.model.PropertyDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.PropertyFileDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.scanner.PropertyFileScannerPlugin;
import com.buschmais.jqassistant.plugin.rdbms.api.model.*;

/**
 * Scans a database schema, the connection properties are taken from a property
 * file following which contains the plugin name.
 */
@Requires(PropertyFileScannerPlugin.class)
public class SchemaScannerPlugin extends AbstractScannerPlugin<FileResource, ConnectionPropertiesDescriptor> {

    public static final String PLUGIN_NAME = "jqassistant.plugin.rdbms";
    public static final String PROPERTIES_SUFFIX = ".properties";

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaScannerPlugin.class);

    /**
     * The supported JDBC properties.
     */
    private enum PluginProperty {
        Driver, Url, User, Password, InfoLevel, BundledDriver;

        /**
         * Check if the property name matches this property.
         * 
         * @param name
         *            The property.
         * @return <code>true</code> if the name matches.
         */
        boolean matches(String name) {
            return this.name().equals(LOWER_UNDERSCORE.to(UPPER_CAMEL, name));
        }
    }

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        String lowerCase = path.toLowerCase();
        return lowerCase.contains(PLUGIN_NAME) && lowerCase.endsWith(PROPERTIES_SUFFIX);
    }

    @Override
    public ConnectionPropertiesDescriptor scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        PropertyFileDescriptor propertyFileDescriptor = scanner.getContext().peek(PropertyFileDescriptor.class);
        Store store = scanner.getContext().getStore();
        ConnectionPropertiesDescriptor connectionPropertiesDescriptor = store.migrate(propertyFileDescriptor, ConnectionPropertiesDescriptor.class);
        String driver = null;
        String url = null;
        String user = null;
        String password = null;
        String infoLevel = InfoLevel.standard.name();
        String bundledDriver = null;
        Properties properties = new Properties();
        for (PropertyDescriptor propertyDescriptor : connectionPropertiesDescriptor.getProperties()) {
            String name = propertyDescriptor.getName();
            String value = propertyDescriptor.getValue();
            if (PluginProperty.Driver.matches(name)) {
                driver = value;
            } else if (PluginProperty.Url.matches(name)) {
                url = value;
            } else if (PluginProperty.User.matches(name)) {
                user = value;
            } else if (PluginProperty.Password.matches(name)) {
                password = value;
            } else if (PluginProperty.InfoLevel.matches(name)) {
                infoLevel = value;
            } else if (PluginProperty.BundledDriver.matches(name)) {
                bundledDriver = value;
            } else {
                properties.setProperty(name, value);
            }
        }
        if (driver == null && url == null) {
            LOGGER.warn(path + " does not contain a driver or url, skipping scan of schema.");
            return connectionPropertiesDescriptor;
        }
        loadDriver(driver);
        Catalog catalog = getCatalog(driver, url, user, password, infoLevel, bundledDriver, properties);
        store(catalog, connectionPropertiesDescriptor, store);
        return connectionPropertiesDescriptor;
    }

    /**
     * Load a class, e.g. the JDBC driver.
     * 
     * @param driver
     *            The class name.
     * @return <code>true</code> if the class could be loaded.
     */
    private <T> Class<T> loadDriver(String driver) throws IOException {
        try {
            return (Class<T>) Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new IOException(driver + " cannot be loaded, skipping scan of schema.", e);
        }
    }

    /**
     * Retrieves the catalog metadata using schema crawler.
     * 
     * @param driver
     *            The driver name.
     * @param url
     *            The url.
     * @param user
     *            The user.
     * @param password
     *            The password.
     * @param properties
     *            The properties to pass to schema crawler.
     * @param infoLevelName
     *            The name of the info level to use.
     * @param bundledDriverName
     *            The name of the bundled driver as provided by schema crawler.
     * @return The catalog.
     * @throws IOException
     *             If retrieval fails.
     */
    private Catalog getCatalog(String driver, String url, String user, String password, String infoLevelName, String bundledDriverName, Properties properties)
            throws IOException {
        // Determine info level
        InfoLevel level = InfoLevel.valueOf(infoLevelName.toLowerCase());
        SchemaInfoLevel schemaInfoLevel = level.getSchemaInfoLevel();
        // Set options
        for (InfoLevelOption option : InfoLevelOption.values()) {
            String value = properties.getProperty(option.getPropertyName());
            if (value != null) {
                LOGGER.info("Setting option " + option.name() + "=" + value);
                option.set(schemaInfoLevel, Boolean.valueOf(value.toLowerCase()));
            }
        }
        SchemaCrawlerOptions options;
        if (bundledDriverName != null) {
            options = getOptions(bundledDriverName, level);
        } else {
            options = new SchemaCrawlerOptions();
        }
        options.setSchemaInfoLevel(schemaInfoLevel);
        Catalog catalog;
        LOGGER.info("Scanning database schemas for '" + url + "' (driver='" + driver + "', user='" + user + "', info level='" + level.name() + "')");
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            catalog = SchemaCrawlerUtility.getCatalog(connection, options);
        } catch (SQLException | SchemaCrawlerException e) {
            throw new IOException(String.format("Cannot scan schema (driver='%s', url='%s', user='%s'", driver, url, user), e);
        }
        return catalog;
    }

    /**
     * Loads the bundled driver options
     * 
     * @param bundledDriverName
     *            The driver name.
     * @param level
     *            The info level.
     * @return The options or <code>null</code>.
     * @throws IOException
     *             If loading fails.
     */
    private SchemaCrawlerOptions getOptions(String bundledDriverName, InfoLevel level) throws IOException {
        for (BundledDriver bundledDriver : BundledDriver.values()) {
            if (bundledDriver.name().toLowerCase().equals(bundledDriverName.toLowerCase())) {
                return bundledDriver.getOptions(level);
            }
        }
        throw new IOException("Unknown bundled driver name '" + bundledDriverName + "', supported values are " + asList(BundledDriver.values()));
    }

    /**
     * Stores the data.
     * 
     * @param catalog
     *            The catalog.
     * @param connectionPropertiesDescriptor
     *            The properties descriptor.
     * @param store
     *            The store.
     */
    private void store(Catalog catalog, ConnectionPropertiesDescriptor connectionPropertiesDescriptor, Store store) {
        Map<String, ColumnTypeDescriptor> columnTypes = new HashMap<>();
        Map<Column, ColumnDescriptor> allColumns = new HashMap<>();
        Set<ForeignKey> allForeignKeys = new HashSet<>();
        for (Schema schema : catalog.getSchemas()) {
            SchemaDescriptor schemaDescriptor = store.create(SchemaDescriptor.class);
            schemaDescriptor.setName(schema.getName());
            connectionPropertiesDescriptor.getSchemas().add(schemaDescriptor);
            // Tables
            createTables(catalog, schema, schemaDescriptor, columnTypes, allColumns, allForeignKeys, store);
            // Sequences
            createSequences(catalog.getSequences(schema), schemaDescriptor, store);
        }
        createForeignKeys(allForeignKeys, allColumns, store);
    }

    /**
     * Create the table descriptors.
     * 
     * @param catalog
     *            The catalog.
     * @param schema
     *            The schema.
     * @param schemaDescriptor
     *            The schema descriptor.
     * @param columnTypes
     *            The cached data types.
     * @param allColumns
     *            The map to collect all columns.
     * @param allForeignKeys
     *            The map to collect all foreign keys.
     * @param store
     *            The store.
     */
    private void createTables(Catalog catalog, Schema schema, SchemaDescriptor schemaDescriptor, Map<String, ColumnTypeDescriptor> columnTypes,
            Map<Column, ColumnDescriptor> allColumns, Set<ForeignKey> allForeignKeys, Store store) {
        for (Table table : catalog.getTables(schema)) {
            TableDescriptor tableDescriptor = getTableDescriptor(table, schemaDescriptor, store);
            Map<String, ColumnDescriptor> localColumns = new HashMap<>();
            for (Column column : table.getColumns()) {
                ColumnDescriptor columnDescriptor = store.create(ColumnDescriptor.class);
                columnDescriptor.setName(column.getName());
                columnDescriptor.setDefaultValue(column.getDefaultValue());
                columnDescriptor.setGenerated(column.isGenerated());
                columnDescriptor.setPartOfIndex(column.isPartOfIndex());
                columnDescriptor.setPartOfPrimaryKey(column.isPartOfPrimaryKey());
                columnDescriptor.setPartOfForeignKey(column.isPartOfForeignKey());
                columnDescriptor.setNullable(column.isNullable());
                columnDescriptor.setAutoIncremented(column.isAutoIncremented());
                columnDescriptor.setSize(column.getSize());
                columnDescriptor.setDecimalDigits(column.getDecimalDigits());
                tableDescriptor.getColumns().add(columnDescriptor);
                ColumnDataType columnDataType = column.getColumnDataType();
                ColumnTypeDescriptor columnTypeDescriptor = getColumnTypeDescriptor(columnDataType, columnTypes, store);
                columnDescriptor.setColumnType(columnTypeDescriptor);
                localColumns.put(column.getName(), columnDescriptor);
                allColumns.put(column, columnDescriptor);
            }
            // Primary key
            PrimaryKey primaryKey = table.getPrimaryKey();
            if (primaryKey != null) {
                PrimaryKeyDescriptor primaryKeyDescriptor = storeIndex(primaryKey, tableDescriptor, localColumns, PrimaryKeyDescriptor.class,
                        PrimaryKeyOnColumnDescriptor.class, store);
                tableDescriptor.setPrimaryKey(primaryKeyDescriptor);
            }
            // Indices
            for (Index index : table.getIndices()) {
                IndexDescriptor indexDescriptor = storeIndex(index, tableDescriptor, localColumns, IndexDescriptor.class, IndexOnColumnDescriptor.class, store);
                tableDescriptor.getIndices().add(indexDescriptor);
            }
            // Trigger
            for (Trigger trigger : table.getTriggers()) {
                TriggerDescriptor triggerDescriptor = store.create(TriggerDescriptor.class);
                triggerDescriptor.setName(trigger.getName());
                triggerDescriptor.setActionCondition(trigger.getActionCondition());
                triggerDescriptor.setActionOrder(trigger.getActionOrder());
                triggerDescriptor.setActionOrientation(trigger.getActionOrientation().name());
                triggerDescriptor.setActionStatement(trigger.getActionStatement());
                triggerDescriptor.setConditionTiming(trigger.getConditionTiming().name());
                triggerDescriptor.setEventManipulationTime(trigger.getEventManipulationType().name());
                tableDescriptor.getTriggers().add(triggerDescriptor);
            }
            allForeignKeys.addAll(table.getForeignKeys());
        }
    }

    /**
     * Create the foreign key descriptors.
     * 
     * @param allForeignKeys
     *            All collected foreign keys.
     * @param allColumns
     *            All collected columns.
     * @param store
     *            The store.
     */
    private void createForeignKeys(Set<ForeignKey> allForeignKeys, Map<Column, ColumnDescriptor> allColumns, Store store) {
        // Foreign keys
        for (ForeignKey foreignKey : allForeignKeys) {
            ForeignKeyDescriptor foreignKeyDescriptor = store.create(ForeignKeyDescriptor.class);
            foreignKeyDescriptor.setName(foreignKey.getName());
            foreignKeyDescriptor.setDeferrability(foreignKey.getDeferrability().name());
            foreignKeyDescriptor.setDeleteRule(foreignKey.getDeleteRule().name());
            foreignKeyDescriptor.setUpdateRule(foreignKey.getUpdateRule().name());
            for (ForeignKeyColumnReference columnReference : foreignKey.getColumnReferences()) {
                ForeignKeyReferenceDescriptor keyReferenceDescriptor = store.create(ForeignKeyReferenceDescriptor.class);
                // foreign key table and column
                Column foreignKeyColumn = columnReference.getForeignKeyColumn();
                ColumnDescriptor foreignKeyColumnDescriptor = allColumns.get(foreignKeyColumn);
                keyReferenceDescriptor.setForeignKeyColumn(foreignKeyColumnDescriptor);
                // primary key table and column
                Column primaryKeyColumn = columnReference.getPrimaryKeyColumn();
                ColumnDescriptor primaryKeyColumnDescriptor = allColumns.get(primaryKeyColumn);
                keyReferenceDescriptor.setPrimaryKeyColumn(primaryKeyColumnDescriptor);
                foreignKeyDescriptor.getForeignKeyReferences().add(keyReferenceDescriptor);
            }
        }
    }

    /**
     * Add the sequences of a schema to the schema descriptor.
     * 
     * @param sequences
     *            The sequences.
     * @param schemaDescriptor
     *            The schema descriptor.
     * @param store
     *            The store.
     */
    private void createSequences(Collection<Sequence> sequences, SchemaDescriptor schemaDescriptor, Store store) {
        for (Sequence sequence : sequences) {
            SequenceDesriptor sequenceDesriptor = store.create(SequenceDesriptor.class);
            sequenceDesriptor.setName(sequence.getName());
            sequenceDesriptor.setIncrement(sequence.getIncrement());
            sequenceDesriptor.setMinimumValue(sequence.getMinimumValue().longValue());
            sequenceDesriptor.setMaximumValue(sequence.getMaximumValue().longValue());
            sequenceDesriptor.setCycle(sequence.isCycle());
            schemaDescriptor.getSequences().add(sequenceDesriptor);
        }
    }

    /**
     * Create a table descriptor for the given table.
     * 
     * @param store
     *            The store
     * @param table
     *            The table
     * @return The table descriptor
     */
    private TableDescriptor getTableDescriptor(Table table, SchemaDescriptor schemaDescriptor, Store store) {
        TableDescriptor tableDescriptor;
        if (table instanceof View) {
            View view = (View) table;
            ViewDescriptor viewDescriptor = store.create(ViewDescriptor.class);
            viewDescriptor.setUpdatable(view.isUpdatable());
            CheckOptionType checkOption = view.getCheckOption();
            if (checkOption != null) {
                viewDescriptor.setCheckOption(checkOption.name());
            }
            schemaDescriptor.getViews().add(viewDescriptor);
            tableDescriptor = viewDescriptor;
        } else {
            tableDescriptor = store.create(TableDescriptor.class);
            schemaDescriptor.getTables().add(tableDescriptor);
        }
        tableDescriptor.setName(table.getName());
        return tableDescriptor;
    }

    /**
     * Stores index data.
     * 
     * @param index
     *            The index.
     * @param tableDescriptor
     *            The table descriptor.
     * @param columns
     *            The cached columns.
     * @param indexType
     *            The index type to create.
     * @param onColumnType
     *            The type representing "on column" to create.
     * @param store
     *            The store.
     * @return The created index descriptor.
     */
    private <I extends IndexDescriptor> I storeIndex(Index index, TableDescriptor tableDescriptor, Map<String, ColumnDescriptor> columns, Class<I> indexType,
            Class<? extends OnColumnDescriptor> onColumnType, Store store) {
        I indexDescriptor = store.create(indexType);
        indexDescriptor.setName(index.getName());
        indexDescriptor.setUnique(index.isUnique());
        indexDescriptor.setCardinality(index.getCardinality());
        indexDescriptor.setIndexType(index.getIndexType().name());
        indexDescriptor.setPages(index.getPages());
        for (IndexColumn indexColumn : index.getColumns()) {
            ColumnDescriptor columnDescriptor = columns.get(indexColumn.getName());
            OnColumnDescriptor onColumnDescriptor = store.create(indexDescriptor, onColumnType, columnDescriptor);
            onColumnDescriptor.setIndexOrdinalPosition(indexColumn.getIndexOrdinalPosition());
            onColumnDescriptor.setSortSequence(indexColumn.getSortSequence().name());
        }
        return indexDescriptor;
    }

    /**
     * Return the column type descriptor for the given data type.
     * 
     * @param columnDataType
     *            The data type.
     * @param columnTypes
     *            The cached data types.
     * @param store
     *            The store.
     * @return The column type descriptor.
     */
    private ColumnTypeDescriptor getColumnTypeDescriptor(ColumnDataType columnDataType, Map<String, ColumnTypeDescriptor> columnTypes, Store store) {
        String databaseSpecificTypeName = columnDataType.getDatabaseSpecificTypeName();
        ColumnTypeDescriptor columnTypeDescriptor = columnTypes.get(databaseSpecificTypeName);
        if (columnTypeDescriptor == null) {
            columnTypeDescriptor = store.find(ColumnTypeDescriptor.class, databaseSpecificTypeName);
            if (columnTypeDescriptor == null) {
                columnTypeDescriptor = store.create(ColumnTypeDescriptor.class);
                columnTypeDescriptor.setDatabaseType(databaseSpecificTypeName);
                columnTypeDescriptor.setAutoIncrementable(columnDataType.isAutoIncrementable());
                columnTypeDescriptor.setCaseSensitive(columnDataType.isCaseSensitive());
                columnTypeDescriptor.setPrecision(columnDataType.getPrecision());
                columnTypeDescriptor.setMinimumScale(columnDataType.getMinimumScale());
                columnTypeDescriptor.setMaximumScale(columnDataType.getMaximumScale());
                columnTypeDescriptor.setFixedPrecisionScale(columnDataType.isFixedPrecisionScale());
                columnTypeDescriptor.setNumericPrecisionRadix(columnDataType.getNumPrecisionRadix());
                columnTypeDescriptor.setUnsigned(columnDataType.isUnsigned());
                columnTypeDescriptor.setUserDefined(columnDataType.isUserDefined());
                columnTypeDescriptor.setNullable(columnDataType.isNullable());
            }
            columnTypes.put(databaseSpecificTypeName, columnTypeDescriptor);
        }
        return columnTypeDescriptor;
    }
}
