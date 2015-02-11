package com.buschmais.jqassistant.plugin.java.impl.scanner;

import java.io.File;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.model.FileContainerDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractDirectoryScannerPlugin;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaClassesDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolver;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolverBuilder;

public class JavaClassesDirectoryScannerPlugin extends AbstractDirectoryScannerPlugin<JavaClassesDirectoryDescriptor> {

    @Override
    protected Scope getRequiredScope() {
        return JavaScope.CLASSPATH;
    }

    @Override
    protected void enterContainer(JavaClassesDirectoryDescriptor javaClassesDirectoryDescriptor, ScannerContext context) {
        context.push(JavaArtifactDescriptor.class, javaClassesDirectoryDescriptor);
        context.push(TypeResolver.class, TypeResolverBuilder.createTypeResolver(context));
    }

    @Override
    protected void leaveContainer(ScannerContext context) {
        context.pop(TypeResolver.class);
        context.pop(JavaArtifactDescriptor.class);
    }

    @Override
    protected JavaClassesDirectoryDescriptor getContainerDescriptor(File classPathDirectory, ScannerContext scannerContext) {
        JavaClassesDirectoryDescriptor javaArtifactDescriptor = scannerContext.peek(JavaClassesDirectoryDescriptor.class);
        Store store = scannerContext.getStore();
        if (javaArtifactDescriptor == null) {
            javaArtifactDescriptor = store.create(JavaClassesDirectoryDescriptor.class);
        }
        return javaArtifactDescriptor;
    }
}
