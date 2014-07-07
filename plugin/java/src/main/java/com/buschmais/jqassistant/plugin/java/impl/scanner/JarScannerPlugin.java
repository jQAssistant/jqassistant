package com.buschmais.jqassistant.plugin.java.impl.scanner;

import java.io.File;
import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractArchiveScannerPlugin;
import com.buschmais.jqassistant.plugin.java.api.model.JarArchiveDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;

public class JarScannerPlugin extends AbstractArchiveScannerPlugin {

    JarArchiveDescriptor jarArchiveDescriptor;

    @Override
    protected String getExtension() {
        return ".jar";
    }

    @Override
    protected Scope createScope(Scope currentScope) {
        return JavaScope.CLASSPATH;
    }

    @Override
    public Iterable<? extends FileDescriptor> scan(File file, String path,
            Scope currentScope, Scanner scanner) throws IOException {
        this.jarArchiveDescriptor = getStore().create(
                JarArchiveDescriptor.class);
        this.jarArchiveDescriptor.setFileName(file.getName());
        return super.scan(file, path, currentScope, scanner);
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
        for (FileDescriptor d : fileDescriptors)
            this.jarArchiveDescriptor.getContents().add(d);

        return fileDescriptors;
    }

    @Override
    protected Iterable<? extends FileDescriptor> afterArchive(
            Iterable<? extends FileDescriptor> fileDescriptors) {
        return fileDescriptors;
    }

}
