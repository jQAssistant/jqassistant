package com.buschmais.jqassistant.plugin.java.impl.scanner;

import java.io.File;

import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.type.ArchiveDescriptor;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractArchiveScannerPlugin;
import com.buschmais.jqassistant.plugin.java.api.model.JarArchiveDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;

public class JarScannerPlugin extends AbstractArchiveScannerPlugin {

    @Override
    protected String getExtension() {
        return ".jar";
    }

    @Override
    protected Scope createScope(Scope currentScope) {
        return JavaScope.CLASSPATH;
    }

    @Override
    protected void beforeArchive(String path, Scope scope) {
    }

    @Override
    protected void beforeEntry(String path, Scope scope) {
    }

    @Override
    protected Iterable<? extends FileDescriptor> afterEntry(
            Iterable<? extends FileDescriptor> fileDescriptors) {
        return fileDescriptors;
    }

    @Override
    protected Iterable<? extends FileDescriptor> afterArchive(
            Iterable<? extends FileDescriptor> fileDescriptors) {
        return fileDescriptors;
    }

    @Override
    protected ArchiveDescriptor createArchiveDescriptor(File file, final String path) {
        JarArchiveDescriptor jarArchiveDescriptor = getStore().create(
                JarArchiveDescriptor.class);
        jarArchiveDescriptor.setFileName(file.getName());
        return jarArchiveDescriptor;
    }

}
