package com.buschmais.jqassistant.plugin.java.impl.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.PropertyDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.java.api.model.PropertyFileDescriptor;

/**
 * Implementation of a
 * {@link com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin}
 * for property files.
 */
@Requires(FileDescriptor.class)
public class PropertyFileScannerPlugin extends AbstractScannerPlugin<FileResource, PropertyFileDescriptor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyFileScannerPlugin.class);

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        return path.toLowerCase().endsWith(".properties");
    }

    @Override
    public PropertyFileDescriptor scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        ScannerContext context = scanner.getContext();
        Store store = context.getStore();
        FileDescriptor fileDescriptor = context.peek(FileDescriptor.class);
        PropertyFileDescriptor propertyFileDescriptor = store.addDescriptorType(fileDescriptor, PropertyFileDescriptor.class);
        Properties properties = new Properties();
        try (InputStream stream = item.createStream()) {
            properties.load(stream);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Cannot load properties from '" + path + "': " + e.getMessage());
        }
        for (String name : properties.stringPropertyNames()) {
            String value = properties.getProperty(name);
            PropertyDescriptor propertyDescriptor = store.create(PropertyDescriptor.class);
            propertyDescriptor.setName(name);
            propertyDescriptor.setValue(value);
            propertyFileDescriptor.getProperties().add(propertyDescriptor);
        }
        return propertyFileDescriptor;
    }

}
