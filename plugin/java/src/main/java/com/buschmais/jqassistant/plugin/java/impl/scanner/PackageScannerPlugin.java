package com.buschmais.jqassistant.plugin.java.impl.scanner;

import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractFileScannerPlugin;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PackageDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.resolver.PackageDescriptorResolver;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;

/**
 * Implementation of the {@link AbstractFileScannerPlugin} for java packages.
 */
public class PackageScannerPlugin extends AbstractFileScannerPlugin<PackageDescriptor> {

    @Override
    protected void initialize() {
    }

    @Override
    public boolean matches(String file, boolean isDirectory) {
        return isDirectory && !"META-INF".equals(file);
    }

    @Override
    public PackageDescriptor scanFile(StreamSource streamSource) throws IOException {
        return null;
    }

    @Override
    public PackageDescriptor scanDirectory(String name) throws IOException {
        String packageName = name.replaceAll("/", ".");
        PackageDescriptorResolver packageDescriptorResolver = new PackageDescriptorResolver(getStore());
        return packageDescriptorResolver.resolve(packageName);
    }
}
