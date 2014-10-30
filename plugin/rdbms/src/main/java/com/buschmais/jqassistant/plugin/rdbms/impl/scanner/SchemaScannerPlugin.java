package com.buschmais.jqassistant.plugin.rdbms.impl.scanner;

import static com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
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
import com.buschmais.jqassistant.plugin.rdbms.api.model.ColumnDescriptor;
import com.buschmais.jqassistant.plugin.rdbms.api.model.ConnectionPropertiesDescriptor;
import com.buschmais.jqassistant.plugin.rdbms.api.model.SchemaDescriptor;
import com.buschmais.jqassistant.plugin.rdbms.api.model.TableDescriptor;

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
        for (Schema schema : catalog.getSchemas()) {
            SchemaDescriptor schemaDescriptor = store.create(SchemaDescriptor.class);
            schemaDescriptor.setName(schema.getName());
            connectionPropertiesDescriptor.getSchemas().add(schemaDescriptor);
            for (Table table : catalog.getTables(schema)) {
                TableDescriptor tableDescriptor = store.create(TableDescriptor.class);
                tableDescriptor.setName(table.getName());
                schemaDescriptor.getTables().add(tableDescriptor);
                for (Column column : table.getColumns()) {
                    ColumnDescriptor columnDescriptor = store.create(ColumnDescriptor.class);
                    columnDescriptor.setName(column.getName());
                    tableDescriptor.getColumns().add(columnDescriptor);
                }
            }
        }
    }
}
