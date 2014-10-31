package com.buschmais.jqassistant.plugin.rdbms.impl.scanner;

import static com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;

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

    private static final String PROPERTY_INFOLEVEL = "info_level";

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaScannerPlugin.class);

    /**
     * The supported JDBC properties.
     */
    private enum JdbcProperties {
        Driver, Url, User, Password;

        /**
         * Check if the property name matches this property.
         * 
         * @param name
         *            The property.
         * @return <code>true</code> if the name matches.
         */
        boolean matches(String name) {
            return this.name().toLowerCase().equals(name.toLowerCase());
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
        Properties properties = new Properties();
        for (PropertyDescriptor propertyDescriptor : connectionPropertiesDescriptor.getProperties()) {
            String name = propertyDescriptor.getName();
            String value = propertyDescriptor.getValue();
            if (JdbcProperties.Driver.matches(name)) {
                driver = value;
            } else if (JdbcProperties.Url.matches(name)) {
                url = value;
            } else if (JdbcProperties.User.matches(name)) {
                user = value;
            } else if (JdbcProperties.Password.matches(name)) {
                password = value;
            } else {
                properties.setProperty(name, value);
            }
        }
        if (driver == null && url == null) {
            LOGGER.warn(path + " does not contain a driver or url, skipping scan of schema.");
            return connectionPropertiesDescriptor;
        }
        if (loadDriver(driver)) {
            Catalog catalog = getCatalog(driver, url, user, password, properties);
            store(catalog, connectionPropertiesDescriptor, store);
        }
        return connectionPropertiesDescriptor;
    }

    /**
     * Load the JDBC driver.
     * 
     * @param driver
     *            The driver name.
     * @return <code>true</code> if the driver could be loaded.
     */
    private boolean loadDriver(String driver) {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            LOGGER.warn(driver + " cannot be loaded, skipping scan of schema.");
            return false;
        }
        return true;
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
     * @return The catalog.
     * @throws IOException
     *             If retrieval fails.
     */
    private Catalog getCatalog(String driver, String url, String user, String password, Properties properties) throws IOException {
        // Determine info level
        String infoLevelName = properties.getProperty(PROPERTY_INFOLEVEL, InfoLevel.Standard.name());
        InfoLevel level = InfoLevel.valueOf(LOWER_CAMEL.to(UPPER_CAMEL, infoLevelName));
        LOGGER.info("Scanning database schemas for '" + url + "' (driver='" + driver + "', user='" + user + "', info level='" + level.name() + "')");
        SchemaCrawlerOptions options = new SchemaCrawlerOptions();
        // Set options
        SchemaInfoLevel schemaInfoLevel = level.getSchemaInfoLevel();
        for (InfoLevelOption option : InfoLevelOption.values()) {
            String value = properties.getProperty(option.getPropertyName());
            if (value != null) {
                LOGGER.info("Setting option " + option.name() + "=" + value);
                option.set(schemaInfoLevel, Boolean.valueOf(value.toLowerCase()));
            }
        }
        options.setSchemaInfoLevel(schemaInfoLevel);
        Catalog catalog;
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            catalog = SchemaCrawlerUtility.getCatalog(connection, options);
        } catch (SQLException | SchemaCrawlerException e) {
            throw new IOException(String.format("Cannot scan schema (driver='%s', url='%s', user='%s'", driver, url, user), e);
        }
        return catalog;
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
        Map<Table, TableDescriptor> allTables = new HashMap<>();
        Map<Column, ColumnDescriptor> allColumns = new HashMap<>();
        Set<ForeignKey> allForeignKeys = new HashSet<>();
        for (Schema schema : catalog.getSchemas()) {
            SchemaDescriptor schemaDescriptor = store.create(SchemaDescriptor.class);
            schemaDescriptor.setName(schema.getName());
            connectionPropertiesDescriptor.getSchemas().add(schemaDescriptor);
            // Tables
            for (Table table : catalog.getTables(schema)) {
                TableDescriptor tableDescriptor = store.create(TableDescriptor.class);
                tableDescriptor.setName(table.getName());
                schemaDescriptor.getTables().add(tableDescriptor);
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
                    storeIndex(primaryKey, tableDescriptor, localColumns, PrimaryKeyDescriptor.class, PrimaryKeyOnColumnDescriptor.class, store);
                }
                // Indices
                for (Index index : table.getIndices()) {
                    storeIndex(index, tableDescriptor, localColumns, IndexDescriptor.class, IndexOnColumnDescriptor.class, store);
                }
                allTables.put(table, tableDescriptor);
                allForeignKeys.addAll(table.getForeignKeys());
            }
            // Sequences
            for (Sequence sequence : catalog.getSequences(schema)) {
                SequenceDesriptor sequenceDesriptor = store.create(SequenceDesriptor.class);
                sequenceDesriptor.setName(sequence.getName());
                sequenceDesriptor.setIncrement(sequence.getIncrement());
                sequenceDesriptor.setMinimumValue(sequence.getMinimumValue());
                sequenceDesriptor.setMaximumValue(sequence.getMaximumValue());
                sequenceDesriptor.setCycle(sequence.isCycle());
                schemaDescriptor.getSequences().add(sequenceDesriptor);
            }
        }
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
                TableDescriptor foreignKeyTableDescriptor = allTables.get(foreignKeyColumn.getParent());
                keyReferenceDescriptor.setForeignKeyTable(foreignKeyTableDescriptor);
                // primary key table and column
                Column primaryKeyColumn = columnReference.getPrimaryKeyColumn();
                ColumnDescriptor primaryKeyColumnDescriptor = allColumns.get(primaryKeyColumn);
                keyReferenceDescriptor.setPrimaryKeyColumn(primaryKeyColumnDescriptor);
                TableDescriptor primaryKeyTableDescriptor = allTables.get(primaryKeyColumn.getParent());
                keyReferenceDescriptor.setPrimaryKeyTable(primaryKeyTableDescriptor);
                foreignKeyDescriptor.getForeignKeyReferences().add(keyReferenceDescriptor);
            }
        }
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
     */
    private void storeIndex(Index index, TableDescriptor tableDescriptor, Map<String, ColumnDescriptor> columns, Class<? extends IndexDescriptor> indexType,
            Class<? extends OnColumnDescriptor> onColumnType, Store store) {
        IndexDescriptor indexDescriptor = store.create(indexType);
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
        tableDescriptor.getIndices().add(indexDescriptor);
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
