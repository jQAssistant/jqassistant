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
    public boolean accepts(File file, String path, Scope scope) throws IOException {
        return file.isFile() && file.getName().endsWith(getExtension());
    }

    @Override
    public FileDescriptor scan(File file, String path, Scope currentScope, Scanner scanner) throws IOException {
        ZipFile zipFile = new ZipFile(file);
        Scope zipScope = createScope(currentScope);
        return scanner.scan(zipFile, path, zipScope);
    }

    protected abstract String getExtension();

    protected abstract Scope createScope(Scope currentScope);

}
