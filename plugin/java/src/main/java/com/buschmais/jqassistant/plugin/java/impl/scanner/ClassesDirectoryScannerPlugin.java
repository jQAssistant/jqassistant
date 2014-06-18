package com.buschmais.jqassistant.plugin.java.impl.scanner;

import java.io.File;
import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.scanner.api.iterable.MappingIterable;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractDirectoryScannerPlugin;
import com.buschmais.jqassistant.plugin.java.api.ClassesDirectory;
import com.buschmais.jqassistant.plugin.java.api.JavaScope;

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
    protected void beforeDirectory(ClassesDirectory classesDirectory, String path) {
        classesDirectory.getDescriptor().setDirectoryName(path);
    }

    @Override
    protected Iterable<? extends FileDescriptor> afterDirectory(final ClassesDirectory classesDirectory, Iterable<? extends FileDescriptor> descriptors) {
        return new MappingIterable<FileDescriptor, FileDescriptor>(descriptors) {
            @Override
            protected FileDescriptor map(FileDescriptor element) throws IOException {
                classesDirectory.getDescriptor().addContains(element);
                return element;
            }
        };
    }
}
