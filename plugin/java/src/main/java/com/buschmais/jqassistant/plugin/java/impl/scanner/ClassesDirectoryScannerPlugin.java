package com.buschmais.jqassistant.plugin.java.impl.scanner;

import java.io.File;
import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.type.DirectoryDescriptor;
import com.buschmais.jqassistant.plugin.common.api.type.ArtifactDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractDirectoryScannerPlugin;
import com.buschmais.jqassistant.plugin.java.api.scanner.ClassesDirectory;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;

public class ClassesDirectoryScannerPlugin extends AbstractDirectoryScannerPlugin<ClassesDirectory> {

    @Override
    public Class<? super ClassesDirectory> getType() {
        return ClassesDirectory.class;
    }

    @Override
    public boolean accepts(ClassesDirectory item, String path, Scope scope) throws IOException {
        return true;
    }

    @Override
    protected File getDirectory(ClassesDirectory item) {
        return item.getDirectory();
    }

    @Override
    protected Scope createScope(Scope currentScope) {
        return JavaScope.CLASSPATH;
    }

    @Override
    protected DirectoryDescriptor beforeDirectory(ClassesDirectory classesDirectory, String path) {
        ArtifactDirectoryDescriptor directoryDescriptor = classesDirectory.getDirectoryDescriptor();
        if (directoryDescriptor == null) {
            directoryDescriptor = getStore().create(ArtifactDirectoryDescriptor.class);
        }
        directoryDescriptor.setFileName(path);
        return directoryDescriptor;
    }
}
