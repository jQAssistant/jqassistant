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
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaSourceFileResolver;

import static com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope.CLASSPATH;

/**
 * Implementation of the {@link AbstractScannerPlugin} for Java packages.
 */
@Requires(DirectoryDescriptor.class)
public class PackageDirectoryScannerPlugin extends AbstractScannerPlugin<DirectoryResource, PackageDescriptor> {

    @Override
    public boolean accepts(DirectoryResource item, String location, Scope scope) throws IOException {
        return (CLASSPATH.equals(scope) && location != null && !location.startsWith("/META-INF"));
    }

    @Override
    public PackageDescriptor scan(DirectoryResource item, String location, Scope scope, Scanner scanner) throws IOException {
        ScannerContext context = scanner.getContext();
        DirectoryDescriptor directoryDescriptor = context.getCurrentDescriptor();
        PackageDescriptor descriptor = context.getStore()
            .addDescriptorType(directoryDescriptor, PackageDescriptor.class);
        String relativePath = location.substring(1);
        String packageName = relativePath.replace("/", ".");
        int separatorIndex = packageName.lastIndexOf('.');
        String name = separatorIndex != -1 ? packageName.substring(separatorIndex + 1) : packageName;
        descriptor.setName(name);
        descriptor.setFullQualifiedName(packageName);
        JavaSourceFileResolver javaSourceFileResolver = context.peekOrDefault(JavaSourceFileResolver.class, null);
        if (javaSourceFileResolver != null) {
            javaSourceFileResolver.resolveSourceFile(relativePath, context)
                .ifPresent(descriptor::setHasSourceFile);
        }
        return descriptor;
    }
}
