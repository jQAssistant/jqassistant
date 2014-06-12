package com.buschmais.jqassistant.plugin.java.impl.scanner;

import static com.buschmais.jqassistant.plugin.java.api.JavaScope.CLASSPATH;
import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PackageDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.resolver.PackageDescriptorResolver;

/**
 * Implementation of the {@link AbstractScannerPlugin} for java packages.
 */
public class PackageDirectoryScannerPlugin extends AbstractScannerPlugin<File> {

    private PackageDescriptorResolver packageDescriptorResolver;

    @Override
    protected void initialize() {
        packageDescriptorResolver = new PackageDescriptorResolver(getStore());
    }

    @Override
    public Class<? super File> getType() {
        return File.class;
    }

    @Override
    public boolean accepts(File item, String path, Scope scope) throws IOException {
        return (CLASSPATH.equals(scope) && item.isDirectory() && path != null && !path.startsWith("/META-INF"));
    }

    @Override
    public Iterable<? extends FileDescriptor> scan(File item, String path, Scope scope, Scanner scanner) throws IOException {
        String packageName = path.substring(1).replaceAll("/", ".");
        PackageDirectoryDescriptor packageDescriptor = packageDescriptorResolver.resolve(packageName, PackageDirectoryDescriptor.class);
        packageDescriptor.setFileName(path);
        return asList(packageDescriptor);
    }

}
