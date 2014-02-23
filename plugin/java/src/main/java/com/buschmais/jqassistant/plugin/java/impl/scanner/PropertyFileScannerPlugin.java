package com.buschmais.jqassistant.plugin.java.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractFileScannerPlugin;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PropertyDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PropertyFileDescriptor;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.util.Properties;

/**
 * Implementation of a {@link AbstractFileScannerPlugin} for property files.
 */
public class PropertyFileScannerPlugin extends AbstractFileScannerPlugin<PropertyFileDescriptor> {

    @Override
    protected void initialize() {
    }

    @Override
    public boolean matches(String file, boolean isDirectory) {
        return !isDirectory && file.endsWith(".properties");
    }

    @Override
    public PropertyFileDescriptor scanFile(StreamSource streamSource) throws IOException {
        Store store = getStore();
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
    public PropertyFileDescriptor scanDirectory(String name) throws IOException {
        return null;
    }
}
