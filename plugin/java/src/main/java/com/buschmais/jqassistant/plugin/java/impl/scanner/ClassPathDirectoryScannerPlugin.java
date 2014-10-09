package com.buschmais.jqassistant.plugin.java.impl.scanner;

import java.io.File;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.type.FileContainerDescriptor;
import com.buschmais.jqassistant.plugin.common.api.type.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.common.api.type.ArtifactDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractDirectoryScannerPlugin;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;

public class ClassPathDirectoryScannerPlugin extends AbstractDirectoryScannerPlugin {

    @Override
    protected Scope getScope() {
        return JavaScope.CLASSPATH;
    }

    @Override
    protected FileContainerDescriptor getContainerDescriptor(File classPathDirectory, ScannerContext scannerContext) {
        ArtifactDescriptor artifactDescriptor = scannerContext.peek(ArtifactDescriptor.class);
        if (artifactDescriptor == null) {
            artifactDescriptor = scannerContext.getStore().create(ArtifactDirectoryDescriptor.class);
        }
        return artifactDescriptor;
    }

}
