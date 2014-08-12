package com.buschmais.jqassistant.plugin.java.impl.scanner;

import java.io.File;
import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.type.FileContainerDescriptor;
import com.buschmais.jqassistant.plugin.common.api.type.ArtifactDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractDirectoryScannerPlugin;
import com.buschmais.jqassistant.plugin.java.api.scanner.ClassPathDirectory;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;

public class ClassPathDirectoryScannerPlugin extends AbstractDirectoryScannerPlugin<ClassPathDirectory> {

    @Override
    public Class<? super ClassPathDirectory> getType() {
        return ClassPathDirectory.class;
    }

    @Override
    public boolean accepts(ClassPathDirectory item, String path, Scope scope) throws IOException {
        return true;
    }

    @Override
    protected File getDirectory(ClassPathDirectory item) {
        return item.getDirectory();
    }

    @Override
    protected FileContainerDescriptor getContainerDescriptor(ClassPathDirectory classPathDirectory) {
        ArtifactDirectoryDescriptor directoryDescriptor = classPathDirectory.getDirectoryDescriptor();
        if (directoryDescriptor == null) {
            directoryDescriptor = getStore().create(ArtifactDirectoryDescriptor.class);
        }
        return directoryDescriptor;
    }

    @Override
    protected Scope createScope(Scope currentScope) {
        return JavaScope.CLASSPATH;
    }

}
