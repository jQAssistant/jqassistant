package com.buschmais.jqassistant.plugin.java.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PrimitiveValueDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PropertiesDescriptor;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.util.Properties;

/**
 * Implementation of the {@link FileScannerPlugin} for property files.
 */
public class PropertyFileScannerPlugin implements FileScannerPlugin<PropertiesDescriptor> {

    @Override
    public boolean matches(String file, boolean isDirectory) {
        return !isDirectory && file.endsWith(".properties");
    }

    @Override
    public PropertiesDescriptor scanFile(Store store, StreamSource streamSource) throws IOException {
        String filename = streamSource.getSystemId();
        PropertiesDescriptor propertiesDescriptor = store.create(PropertiesDescriptor.class, filename);
        Properties properties = new Properties();
        properties.load(streamSource.getInputStream());
        for (String name : properties.stringPropertyNames()) {
            String value = properties.getProperty(name);
            PrimitiveValueDescriptor primitiveValueDescriptor = store.create(PrimitiveValueDescriptor.class, filename + ":" + name);
            primitiveValueDescriptor.setName(name);
            primitiveValueDescriptor.setValue(value);
            propertiesDescriptor.getProperties().add(primitiveValueDescriptor);
        }
        return propertiesDescriptor;
    }

    @Override
    public PropertiesDescriptor scanDirectory(Store store, String name) throws IOException {
        return null;
    }
}
