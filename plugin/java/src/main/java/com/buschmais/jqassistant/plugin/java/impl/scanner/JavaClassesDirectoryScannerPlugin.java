package com.buschmais.jqassistant.plugin.java.impl.scanner;

import java.io.File;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
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
    protected Scope createScope(Scope currentScope, ScannerContext context) {
        context.push(TypeResolver.class, TypeResolverBuilder.createTypeResolver(context));
        return currentScope;
    }

    @Override
    protected void destroyScope(ScannerContext context) {
        context.pop(TypeResolver.class);
    }

    @Override
    protected JavaClassesDirectoryDescriptor getContainerDescriptor(File classPathDirectory, ScannerContext scannerContext) {
        return scannerContext.getStore().create(JavaClassesDirectoryDescriptor.class);
    }
}
