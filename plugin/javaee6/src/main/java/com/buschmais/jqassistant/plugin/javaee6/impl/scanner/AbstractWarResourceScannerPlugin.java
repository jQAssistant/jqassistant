package com.buschmais.jqassistant.plugin.javaee6.impl.scanner;

import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractResourceScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.Resource;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaClassesDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolver;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolverBuilder;
import com.buschmais.jqassistant.plugin.javaee6.api.model.WebApplicationArchiveDescriptor;

public abstract class AbstractWarResourceScannerPlugin<R extends Resource, D extends Descriptor> extends AbstractResourceScannerPlugin<R, D> {

    @Override
    public Class<? extends R> getType() {
        return super.getTypeParameter(AbstractWarResourceScannerPlugin.class, 0);
    }

    @Override
    public Class<? extends D> getDescriptorType() {
        return super.getTypeParameter(AbstractWarResourceScannerPlugin.class, 1);
    }

    @Override
    public D scan(R item, String path, Scope scope, Scanner scanner) throws IOException {
        ScannerContext context = scanner.getContext();
        WebApplicationArchiveDescriptor archiveDescriptor = context.peek(WebApplicationArchiveDescriptor.class);
        JavaClassesDirectoryDescriptor classesDirectory = archiveDescriptor.getClassesDirectory();
        if (classesDirectory == null) {
            classesDirectory = context.getStore().create(JavaClassesDirectoryDescriptor.class);
            archiveDescriptor.setClassesDirectory(classesDirectory);
        }
        context.push(JavaArtifactFileDescriptor.class, classesDirectory);
        TypeResolver typeResolver = TypeResolverBuilder.createTypeResolver(context);
        context.push(TypeResolver.class, typeResolver);
        D fileDescriptor;
        try {
            fileDescriptor = scan(item, path, classesDirectory, scanner);
        } finally {
            context.pop(TypeResolver.class);
            context.pop(JavaArtifactFileDescriptor.class);
        }
        return fileDescriptor;
    }

    protected abstract D scan(R item, String path, JavaClassesDirectoryDescriptor classesDirectory, Scanner scanner) throws IOException;

}
