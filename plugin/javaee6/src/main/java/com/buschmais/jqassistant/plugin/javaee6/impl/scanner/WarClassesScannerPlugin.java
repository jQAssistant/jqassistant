package com.buschmais.jqassistant.plugin.javaee6.impl.scanner;

import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractResourceScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.Resource;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaClassesDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolver;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolverBuilder;
import com.buschmais.jqassistant.plugin.javaee6.api.model.WarArchiveDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.scanner.WebApplicationScope;

public class WarClassesScannerPlugin extends AbstractResourceScannerPlugin<Resource, FileDescriptor> {

    public static final String PREFIX = "/WEB-INF/classes";

    @Override
    public boolean accepts(Resource item, String path, Scope scope) throws IOException {
        return WebApplicationScope.WAR.equals(scope) && path.startsWith(PREFIX);
    }

    @Override
    public FileDescriptor scan(Resource item, String path, Scope scope, Scanner scanner) throws IOException {
        ScannerContext context = scanner.getContext();
        WarArchiveDescriptor archiveDescriptor = context.peek(WarArchiveDescriptor.class);
        JavaClassesDirectoryDescriptor classesDirectory = archiveDescriptor.getClassesDirectory();
        if (classesDirectory == null) {
            classesDirectory = context.getStore().create(JavaClassesDirectoryDescriptor.class);
            archiveDescriptor.setClassesDirectory(classesDirectory);
        }
        if (PREFIX.equals(path)) {
            return classesDirectory;
        }
        context.push(JavaArtifactDescriptor.class, classesDirectory);
        TypeResolver typeResolver = TypeResolverBuilder.createTypeResolver(context);
        context.push(TypeResolver.class, typeResolver);
        FileDescriptor fileDescriptor;
        try {
            String resourcePath = path.substring(PREFIX.length());
            fileDescriptor = scanner.scan(item, resourcePath, JavaScope.CLASSPATH);
            classesDirectory.getContains().add(toFileDescriptor(item, fileDescriptor, resourcePath, context));
        } finally {
            context.pop(TypeResolver.class);
            context.pop(JavaArtifactDescriptor.class);
        }
        return fileDescriptor;
    }
}
