package com.buschmais.jqassistant.plugin.rdbms.impl.scanner;

import static com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import schemacrawler.schema.*;
import schemacrawler.schemacrawler.IncludeAll;
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
        return lowerCase.contains(PLUGIN_NAME) && lowerCase.endsWith(".properties");
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
            Catalog catalog = getCatalog(driver, url, user, password);
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

    private Catalog getCatalog(String driver, String url, String user, String password) throws IOException {
        Catalog catalog;
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            SchemaCrawlerOptions options = new SchemaCrawlerOptions();
            SchemaInfoLevel infoLevel = SchemaInfoLevel.standard();
            options.setSchemaInfoLevel(infoLevel);
            options.setSequenceInclusionRule(new IncludeAll());
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
        for (Schema schema : catalog.getSchemas()) {
            SchemaDescriptor schemaDescriptor = store.create(SchemaDescriptor.class);
            schemaDescriptor.setName(schema.getName());
            connectionPropertiesDescriptor.getSchemas().add(schemaDescriptor);
            // Tables
            for (Table table : catalog.getTables(schema)) {
                TableDescriptor tableDescriptor = store.create(TableDescriptor.class);
                tableDescriptor.setName(table.getName());
                schemaDescriptor.getTables().add(tableDescriptor);
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
                }
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
