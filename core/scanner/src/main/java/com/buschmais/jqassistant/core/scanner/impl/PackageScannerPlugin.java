package com.buschmais.jqassistant.core.scanner.impl;

import com.buschmais.jqassistant.core.model.api.descriptor.PackageDescriptor;
import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;
import com.buschmais.jqassistant.core.scanner.impl.resolver.PackageDescriptorResolver;
import com.buschmais.jqassistant.core.store.api.Store;

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
    public PackageDescriptor scanFile(Store store, InputStreamSource streamSource) throws IOException {
        return null;
    }

    @Override
    public PackageDescriptor scanDirectory(Store store, String name) throws IOException {
        String packageName = name.replaceAll("/", ".");
     /*
        PackageDescriptorResolver packageDescriptorResolver = new PackageDescriptorResolver(store);
        return packageDescriptorResolver.resolve(packageName);
        */
        PackageDescriptorResolver packageDescriptorResolver = new PackageDescriptorResolver(store);
        return packageDescriptorResolver.resolve(packageName);
    }
}
