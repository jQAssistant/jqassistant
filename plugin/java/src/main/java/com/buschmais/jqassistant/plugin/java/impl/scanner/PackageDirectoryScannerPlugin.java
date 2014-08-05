package com.buschmais.jqassistant.plugin.java.impl.scanner;

import static com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope.CLASSPATH;

import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.type.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileSystemResource;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.java.api.model.PackageDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.scanner.resolver.PackageDescriptorResolver;

/**
 * Implementation of the {@link AbstractScannerPlugin} for java packages.
 */
public class PackageDirectoryScannerPlugin extends AbstractScannerPlugin<FileSystemResource> {

    private PackageDescriptorResolver packageDescriptorResolver;

    @Override
    protected void initialize() {
        packageDescriptorResolver = new PackageDescriptorResolver(getStore());
    }

    @Override
    public Class<? super FileSystemResource> getType() {
        return FileSystemResource.class;
    }

    @Override
    public boolean accepts(FileSystemResource item, String path, Scope scope) throws IOException {
        return (CLASSPATH.equals(scope) && item.isDirectory() && path != null && !path.startsWith("/META-INF"));
    }

    @Override
    public FileDescriptor scan(FileSystemResource item, String path, Scope scope, Scanner scanner) throws IOException {
        String packageName = path.substring(1).replaceAll("/", ".");
        PackageDirectoryDescriptor packageDescriptor = packageDescriptorResolver.resolve(packageName, PackageDirectoryDescriptor.class);
        return packageDescriptor;
    }

}
