package com.buschmais.jqassistant.plugin.java.impl.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.java.api.model.PropertyDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.PropertyFileDescriptor;

/**
 * Implementation of a
 * {@link com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractScannerPlugin}
 * for property files.
 */
public class PropertyFileScannerPlugin extends AbstractScannerPlugin<FileResource, PropertyFileDescriptor> {

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        return path.toLowerCase().endsWith(".properties");
    }

    @Override
    public PropertyFileDescriptor scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        Store store = scanner.getContext().getStore();
        PropertyFileDescriptor propertyFileDescriptor = store.create(PropertyFileDescriptor.class);
        Properties properties = new Properties();
        try (InputStream stream = item.createStream()) {
            properties.load(stream);
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
