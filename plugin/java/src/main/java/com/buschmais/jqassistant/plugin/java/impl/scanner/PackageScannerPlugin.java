package com.buschmais.jqassistant.plugin.java.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PackageDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.resolver.PackageDescriptorResolver;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;

/**
 * Implementation of the {@link com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin} for java packages.
 */
public class PackageScannerPlugin implements FileScannerPlugin<PackageDescriptor> {

    @Override
    public boolean matches(String file, boolean isDirectory) {
        return isDirectory && !"META-INF".equals(file);
    }

    @Override
    public PackageDescriptor scanFile(Store store, StreamSource streamSource) throws IOException {
        return null;
    }

    @Override
    public PackageDescriptor scanDirectory(Store store, String name) throws IOException {
        String packageName = name.replaceAll("/", ".");
        PackageDescriptorResolver packageDescriptorResolver = new PackageDescriptorResolver(store);
        return packageDescriptorResolver.resolve(packageName);
    }
}
