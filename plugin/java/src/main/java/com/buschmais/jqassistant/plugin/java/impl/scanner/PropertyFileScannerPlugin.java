package com.buschmais.jqassistant.plugin.java.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PropertyDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PropertyFileDescriptor;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.util.Properties;

/**
 * Implementation of the {@link FileScannerPlugin} for property files.
 */
public class PropertyFileScannerPlugin implements FileScannerPlugin<PropertyFileDescriptor> {

    @Override
    public boolean matches(String file, boolean isDirectory) {
        return !isDirectory && file.endsWith(".properties");
    }

    @Override
    public PropertyFileDescriptor scanFile(Store store, StreamSource streamSource) throws IOException {
        PropertyFileDescriptor propertyFileDescriptor = store.create(PropertyFileDescriptor.class);
        String filename = streamSource.getSystemId();
        propertyFileDescriptor.setFileName(filename);
        Properties properties = new Properties();
        properties.load(streamSource.getInputStream());
        for (String name : properties.stringPropertyNames()) {
            String value = properties.getProperty(name);
            PropertyDescriptor propertyDescriptor = store.create(PropertyDescriptor.class);
            propertyDescriptor.setName(name);
            propertyDescriptor.setValue(value);
            propertyFileDescriptor.getProperties().add(propertyDescriptor);
        }
        return propertyFileDescriptor;
    }

    @Override
    public PropertyFileDescriptor scanDirectory(Store store, String name) throws IOException {
        return null;
    }
}
