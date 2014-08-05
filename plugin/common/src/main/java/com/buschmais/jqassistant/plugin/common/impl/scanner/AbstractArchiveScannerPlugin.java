package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.type.FileDescriptor;

public abstract class AbstractArchiveScannerPlugin extends AbstractScannerPlugin<File> {

    @Override
    protected void initialize() {
    }

    @Override
    public Class<File> getType() {
        return File.class;
    }

    @Override
    public boolean accepts(File item, String path, Scope scope) throws IOException {
        return item.isFile() && item.getName().endsWith(getExtension());
    }

    @Override
    public FileDescriptor scan(File file, final String path, final Scope currentScope, final Scanner scanner) throws IOException {
        ZipFile zipFile = new ZipFile(file);
        Scope zipScope = createScope(currentScope);
        FileDescriptor fileDescriptor = scanner.scan(zipFile, path, zipScope);
        return afterArchive(fileDescriptor);
    }

    protected abstract String getExtension();

    protected abstract Scope createScope(Scope currentScope);

    protected abstract FileDescriptor afterArchive(FileDescriptor fileDescriptor);

}
