package com.buschmais.jqassistant.plugin.rdbms.impl.scanner;

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

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.rdbms.api.model.*;

/**
 * Abstract base class for database schema scanners.
 */
public abstract class AbstractSchemaScannerPlugin<I, D extends ConnectionDescriptor> extends AbstractScannerPlugin<I, D> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSchemaScannerPlugin.class);

    @Override
    public Class<? extends I> getType() {
        return getTypeParameter(AbstractSchemaScannerPlugin.class, 0);
    }

    @Override
    public Class<? extends D> getDescriptorType() {
        return getTypeParameter(AbstractSchemaScannerPlugin.class, 1);
    }

    /**
     * Scans the connection identified by the given parameters.
     *
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
     * @param store
     *            The store.
     * @return The list of created schema descriptors.
     * @throws java.io.IOException
     *             If retrieval fails.
     */
    protected List<SchemaDescriptor> scanConnection(String url, String user, String password, String infoLevelName, String bundledDriverName,
            Properties properties, Store store) throws IOException {
        Catalog catalog = getCatalog(url, user, password, infoLevelName, bundledDriverName, properties);
        return createSchemas(catalog, store);
    }

    /**
     * Retrieves the catalog metadata using schema crawler.
     *
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
     * @throws java.io.IOException
     *             If retrieval fails.
     */
    protected Catalog getCatalog(String url, String user, String password, String infoLevelName, String bundledDriverName, Properties properties)
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
        LOGGER.info("Scanning database schemas on '" + url + "' (user='" + user + "', info level='" + level.name() + "')");
        Catalog catalog;
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            catalog = SchemaCrawlerUtility.getCatalog(connection, options);
        } catch (SQLException | SchemaCrawlerException e) {
            throw new IOException(String.format("Cannot scan schema (url='%s', user='%s'", url, user), e);
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
     * @throws java.io.IOException
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
     * @param store
     *            The store.
     * @return The list of created schema descriptors.
     * @throws java.io.IOException
     *             If an error occurs.
     */
    private List<SchemaDescriptor> createSchemas(Catalog catalog, Store store) throws IOException {
        List<SchemaDescriptor> schemaDescriptors = new ArrayList<>();
        Map<String, ColumnTypeDescriptor> columnTypes = new HashMap<>();
        Map<Column, ColumnDescriptor> allColumns = new HashMap<>();
        Set<ForeignKey> allForeignKeys = new HashSet<>();
        for (Schema schema : catalog.getSchemas()) {
            SchemaDescriptor schemaDescriptor = store.create(SchemaDescriptor.class);
            schemaDescriptor.setName(schema.getName());
            // Tables
            createTables(catalog, schema, schemaDescriptor, columnTypes, allColumns, allForeignKeys, store);
            // Sequences
            createSequences(catalog.getSequences(schema), schemaDescriptor, store);
            // Procedures and Functions
            createRoutines(catalog.getRoutines(schema), schemaDescriptor, columnTypes, store);
            schemaDescriptors.add(schemaDescriptor);
        }
        // Foreign keys
        createForeignKeys(allForeignKeys, allColumns, store);
        return schemaDescriptors;
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
                ColumnDescriptor columnDescriptor = createColumnDescriptor(column, ColumnDescriptor.class, columnTypes, store);
                columnDescriptor.setDefaultValue(column.getDefaultValue());
                columnDescriptor.setGenerated(column.isGenerated());
                columnDescriptor.setPartOfIndex(column.isPartOfIndex());
                columnDescriptor.setPartOfPrimaryKey(column.isPartOfPrimaryKey());
                columnDescriptor.setPartOfForeignKey(column.isPartOfForeignKey());
                columnDescriptor.setAutoIncremented(column.isAutoIncremented());
                tableDescriptor.getColumns().add(columnDescriptor);
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
     * Create a column descriptor.
     *
     * @param column
     *            The column.
     * @param descriptorType
     *            The type to create.
     * @param columnTypes
     *            The column types.
     * @param store
     *            The store.
     * @return The column descriptor.
     */
    private <T extends BaseColumnDescriptor> T createColumnDescriptor(BaseColumn column, Class<T> descriptorType,
            Map<String, ColumnTypeDescriptor> columnTypes, Store store) {
        T columnDescriptor = store.create(descriptorType);
        columnDescriptor.setName(column.getName());
        columnDescriptor.setNullable(column.isNullable());
        columnDescriptor.setSize(column.getSize());
        columnDescriptor.setDecimalDigits(column.getDecimalDigits());
        ColumnDataType columnDataType = column.getColumnDataType();
        ColumnTypeDescriptor columnTypeDescriptor = getColumnTypeDescriptor(columnDataType, columnTypes, store);
        columnDescriptor.setColumnType(columnTypeDescriptor);
        return columnDescriptor;
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
     * Create routines, i.e. functions and procedures.
     *
     * @param routines
     *            The routines.
     * @param schemaDescriptor
     *            The schema descriptor.
     * @param columnTypes
     *            The column types.
     * @param store
     *            The store.
     * @throws java.io.IOException
     *             If an unsupported routine type has been found.
     */
    private void createRoutines(Collection<Routine> routines, SchemaDescriptor schemaDescriptor, Map<String, ColumnTypeDescriptor> columnTypes, Store store)
            throws IOException {
        for (Routine routine : routines) {
            RoutineDescriptor routineDescriptor;
            String returnType;
            switch (routine.getRoutineType()) {
            case procedure:
                routineDescriptor = store.create(ProcedureDescriptor.class);
                returnType = ((ProcedureReturnType) routine.getReturnType()).name();
                schemaDescriptor.getProcedures().add((ProcedureDescriptor) routineDescriptor);
                break;
            case function:
                routineDescriptor = store.create(FunctionDescriptor.class);
                returnType = ((FunctionReturnType) routine.getReturnType()).name();
                schemaDescriptor.getFunctions().add((FunctionDescriptor) routineDescriptor);
                break;
            case unknown:
                routineDescriptor = store.create(RoutineDescriptor.class);
                returnType = null;
                schemaDescriptor.getUnknownRoutines().add(routineDescriptor);
                break;
            default:
                throw new IOException("Unsupported routine type " + routine.getRoutineType());
            }
            routineDescriptor.setName(routine.getName());
            routineDescriptor.setReturnType(returnType);
            routineDescriptor.setBodyType(routine.getRoutineBodyType().name());
            routineDescriptor.setDefinition(routine.getDefinition());
            for (RoutineColumn<? extends Routine> routineColumn : routine.getColumns()) {
                RoutineColumnDescriptor columnDescriptor = createColumnDescriptor(routineColumn, RoutineColumnDescriptor.class, columnTypes, store);
                routineDescriptor.getColumns().add(columnDescriptor);
                RoutineColumnType columnType = routineColumn.getColumnType();
                if (columnType instanceof ProcedureColumnType) {
                    ProcedureColumnType procedureColumnType = (ProcedureColumnType) columnType;
                    columnDescriptor.setType(procedureColumnType.name());
                } else if (columnType instanceof FunctionColumnType) {
                    FunctionColumnType functionColumnType = (FunctionColumnType) columnType;
                    columnDescriptor.setType(functionColumnType.name());
                } else {
                    throw new IOException("Unsupported routine column type " + columnType.getClass().getName());
                }
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
