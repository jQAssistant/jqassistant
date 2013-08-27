package com.buschmais.jqassistant.core.scanner.impl;

import com.buschmais.jqassistant.core.model.api.descriptor.PackageDescriptor;
import com.buschmais.jqassistant.core.scanner.api.ArtifactScannerPlugin;
import com.buschmais.jqassistant.core.store.api.Store;

import java.io.IOException;

/**
 * Implementation of the {@link ArtifactScannerPlugin} for java packages.
 */
public class PackageScannerPlugin implements ArtifactScannerPlugin<PackageDescriptor> {

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
        PackageDescriptor packageDescriptor = store.find(PackageDescriptor.class, packageName);
        if (packageDescriptor == null) {
            packageDescriptor = store.create(PackageDescriptor.class, packageName);
        }
        return packageDescriptor;
    }
}
