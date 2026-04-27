package com.buschmais.jqassistant.plugin.java.impl.scanner;

import java.io.File;
import java.util.Arrays;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractDirectoryScannerPlugin;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.ArtifactScopedTypeResolver;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolver;

/**
 * A scanner plugin for directories containing java classes.
 */
public class JavaClassesDirectoryScannerPlugin
        extends AbstractDirectoryScannerPlugin<JavaArtifactFileDescriptor> {

    @Override
    protected Scope getRequiredScope() {
        return JavaScope.CLASSPATH;
    }

    @Override
    protected void enterContainer(File directory, JavaArtifactFileDescriptor javaClassesDirectoryDescriptor, ScannerContext context) {
        context.push(TypeResolver.class, new ArtifactScopedTypeResolver(javaClassesDirectoryDescriptor));
    }

    @Override
    protected void leaveContainer(File directory, JavaArtifactFileDescriptor javaClassesDirectoryDescriptor, ScannerContext context) {
        context.pop(TypeResolver.class);
    }

    @Override
    protected JavaArtifactFileDescriptor getContainerDescriptor(File classPathDirectory, ScannerContext scannerContext) {
        JavaArtifactFileDescriptor javaArtifactDescriptor = scannerContext.peekOrDefault(JavaArtifactFileDescriptor.class, null);
        Store store = scannerContext.getStore();
        if (javaArtifactDescriptor == null) {
            return store.create(JavaArtifactFileDescriptor.class);
        }
        if (JavaArtifactFileDescriptor.class.isAssignableFrom(javaArtifactDescriptor.getClass())) {
            return JavaArtifactFileDescriptor.class.cast(javaArtifactDescriptor);
        }
        throw new IllegalStateException("Expected an instance of " + JavaArtifactFileDescriptor.class.getName() + " but got "
                + Arrays.asList(javaArtifactDescriptor.getClass().getInterfaces()));
    }
}
