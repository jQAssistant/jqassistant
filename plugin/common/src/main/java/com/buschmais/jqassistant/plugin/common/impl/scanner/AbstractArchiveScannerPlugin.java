package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.IOException;
import java.util.zip.ZipFile;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.model.ArchiveDescriptor;
import com.buschmais.jqassistant.core.store.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.VirtualFile;

public abstract class AbstractArchiveScannerPlugin extends AbstractScannerPlugin<VirtualFile> {

    @Override
    public Class<? extends VirtualFile> getType() {
        return VirtualFile.class;
    }

    @Override
    public boolean accepts(VirtualFile file, String path, Scope scope) throws IOException {
        return path.endsWith(getExtension());
    }

    @Override
    public FileDescriptor scan(VirtualFile file, String path, Scope currentScope, Scanner scanner) throws IOException {
        Scope zipScope = createScope(currentScope);
        ArchiveDescriptor archive = createArchive(file, path, scanner.getContext());
        ZipFile zipFile = new ZipFile(file.getFile());
        scanner.getContext().push(ArchiveDescriptor.class, archive);
        try {
            scanner.scan(zipFile, path, zipScope);
        } finally {
            scanner.getContext().pop(ArchiveDescriptor.class);
        }
        return archive;
    }

    protected abstract String getExtension();

    protected abstract Scope createScope(Scope currentScope);

    protected abstract ArchiveDescriptor createArchive(VirtualFile file, String path, ScannerContext scannerContext);
}
