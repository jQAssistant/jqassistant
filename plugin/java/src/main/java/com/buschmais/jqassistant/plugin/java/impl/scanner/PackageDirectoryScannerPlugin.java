package com.buschmais.jqassistant.plugin.java.impl.scanner;

import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.DirectoryDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.DirectoryResource;
import com.buschmais.jqassistant.plugin.java.api.model.PackageDescriptor;

import static com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope.CLASSPATH;

/**
 * Implementation of the {@link AbstractScannerPlugin} for Java packages.
 */
@Requires(DirectoryDescriptor.class)
public class PackageDirectoryScannerPlugin extends AbstractScannerPlugin<DirectoryResource, PackageDescriptor> {

    @Override
    public boolean accepts(DirectoryResource item, String path, Scope scope) throws IOException {
        return (CLASSPATH.equals(scope) && path != null && !path.startsWith("/META-INF"));
    }

    @Override
    public PackageDescriptor scan(DirectoryResource item, String path, Scope scope, Scanner scanner) throws IOException {
        ScannerContext context = scanner.getContext();
        DirectoryDescriptor directoryDescriptor = context.peek(DirectoryDescriptor.class);
        PackageDescriptor descriptor = context.getStore().addDescriptorType(directoryDescriptor, PackageDescriptor.class);
        String packageName = path.substring(1).replaceAll("/", ".");
        String name;
        int separatorIndex = packageName.lastIndexOf('.');
        if (separatorIndex != -1) {
            name = packageName.substring(separatorIndex + 1);
        } else {
            name = packageName;
        }
        descriptor.setName(name);
        descriptor.setFullQualifiedName(packageName);
        return descriptor;
    }

}
