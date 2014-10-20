package com.buschmais.jqassistant.plugin.java.impl.scanner;

import static com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope.CLASSPATH;

import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.DirectoryResource;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.java.api.model.PackageDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.scanner.resolver.PackageDescriptorResolver;

/**
 * Implementation of the {@link AbstractScannerPlugin} for java packages.
 */
public class PackageDirectoryScannerPlugin extends AbstractScannerPlugin<DirectoryResource, PackageDirectoryDescriptor> {

    private PackageDescriptorResolver packageDescriptorResolver = new PackageDescriptorResolver();

    @Override
    public boolean accepts(DirectoryResource item, String path, Scope scope) throws IOException {
        return (CLASSPATH.equals(scope) && path != null && !path.startsWith("/META-INF"));
    }

    @Override
    public PackageDirectoryDescriptor scan(DirectoryResource item, String path, Scope scope, Scanner scanner) throws IOException {
        String packageName = path.substring(1).replaceAll("/", ".");
        PackageDirectoryDescriptor packageDescriptor = packageDescriptorResolver.resolve(packageName, PackageDirectoryDescriptor.class, scanner.getContext());
        return packageDescriptor;
    }

}
