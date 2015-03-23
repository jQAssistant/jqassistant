package com.buschmais.jqassistant.plugin.java.impl.scanner;

import java.io.File;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractDirectoryScannerPlugin;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaClassesDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;

/**
 * A scanner plugin for directories containing java classes.
 */
public class JavaClassesDirectoryScannerPlugin extends AbstractDirectoryScannerPlugin<JavaClassesDirectoryDescriptor> {

    @Override
    protected Scope getRequiredScope() {
        return JavaScope.CLASSPATH;
    }

    @Override
    protected void enterContainer(File directory, JavaClassesDirectoryDescriptor javaClassesDirectoryDescriptor, ScannerContext context) {
        context.push(JavaArtifactFileDescriptor.class, javaClassesDirectoryDescriptor);
    }

    @Override
    protected void leaveContainer(File directory, JavaClassesDirectoryDescriptor javaClassesDirectoryDescriptor, ScannerContext context) {
        context.pop(JavaArtifactFileDescriptor.class);
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
