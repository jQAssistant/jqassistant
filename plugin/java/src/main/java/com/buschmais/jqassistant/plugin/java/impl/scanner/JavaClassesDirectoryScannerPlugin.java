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

public class JavaClassesDirectoryScannerPlugin extends AbstractDirectoryScannerPlugin {

    @Override
    protected Scope getRequiredScope() {
        return JavaScope.CLASSPATH;
    }

    @Override
    protected FileContainerDescriptor getContainerDescriptor(File classPathDirectory, ScannerContext scannerContext) {
        ArtifactFileDescriptor artifactDescriptor = scannerContext.peek(JavaArtifactDescriptor.class);
        if (artifactDescriptor == null) {
            artifactDescriptor = scannerContext.getStore().create(JavaClassesDirectoryDescriptor.class);
        }
        return artifactDescriptor;
    }
}
