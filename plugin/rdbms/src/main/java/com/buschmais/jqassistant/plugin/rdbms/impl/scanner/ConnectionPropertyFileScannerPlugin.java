package com.buschmais.jqassistant.plugin.rdbms.impl.scanner;

import static com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import schemacrawler.tools.options.InfoLevel;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.PropertyDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.java.api.model.PropertyFileDescriptor;
import com.buschmais.jqassistant.plugin.rdbms.api.model.ConnectionDescriptor;
import com.buschmais.jqassistant.plugin.rdbms.api.model.SchemaDescriptor;

/**
 * Scans a database schema, the connection properties are taken from a property
 * file following which contains the plugin name.
 */
@Requires(PropertyFileDescriptor.class)
public class ConnectionPropertyFileScannerPlugin extends AbstractSchemaScannerPlugin<FileResource, ConnectionDescriptor, ConnectionPropertyFileScannerPlugin> {

    public static final String PLUGIN_NAME = "jqassistant.plugin.rdbms";
    public static final String PROPERTIES_SUFFIX = ".properties";

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPropertyFileScannerPlugin.class);

    @Override
    protected ConnectionPropertyFileScannerPlugin getThis() {
        return this;
    }

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
    protected boolean doAccepts(FileResource item, String path, Scope scope) throws IOException {
        String lowerCase = path.toLowerCase();
        return lowerCase.contains(PLUGIN_NAME) && lowerCase.endsWith(PROPERTIES_SUFFIX);
    }

    @Override
    public ConnectionDescriptor scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        PropertyFileDescriptor propertyFileDescriptor = scanner.getContext().peek(PropertyFileDescriptor.class);
        List<PropertyDescriptor> propertyDescriptors = propertyFileDescriptor.getProperties();
        String driver = null;
        String url = null;
        String user = null;
        String password = null;
        String infoLevel = InfoLevel.standard.name();
        String bundledDriver = null;
        Properties properties = new Properties();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
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
        Store store = scanner.getContext().getStore();
        ConnectionDescriptor connectionDescriptor = store.addDescriptorType(propertyFileDescriptor, ConnectionDescriptor.class);
        if (url == null) {
            LOGGER.warn(path + " does not contain a driver or url, skipping scan of schema.");
            return connectionDescriptor;
        }
        loadDriver(driver);
        List<SchemaDescriptor> schemaDescriptors = scanConnection(url, user, password, infoLevel, bundledDriver, properties, store);
        connectionDescriptor.getSchemas().addAll(schemaDescriptors);
        return connectionDescriptor;
    }

    /**
     * Load a class, e.g. the JDBC driver.
     * 
     * @param driver
     *            The class name.
     */
    private void loadDriver(String driver) throws IOException {
        if (driver != null) {
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException e) {
                throw new IOException(driver + " cannot be loaded, skipping scan of schema.", e);
            }
        }
    }

}
