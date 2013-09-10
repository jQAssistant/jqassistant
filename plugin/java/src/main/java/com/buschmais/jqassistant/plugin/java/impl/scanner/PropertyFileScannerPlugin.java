package com.buschmais.jqassistant.plugin.java.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PackageDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PropertiesDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.resolver.PackageDescriptorResolver;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;

/**
 * Implementation of the {@link FileScannerPlugin} for property files.
 */
public class PropertyFileScannerPlugin implements FileScannerPlugin<PropertiesDescriptor> {

    @Override
    public boolean matches(String file, boolean isDirectory) {
        return isDirectory && !"META-INF".equals(file);
    }

    @Override
    public PropertiesDescriptor scanFile(Store store, StreamSource streamSource) throws IOException {
        return null;
    }

    @Override
    public PropertiesDescriptor scanDirectory(Store store, String name) throws IOException {
        return null;
    }
}
