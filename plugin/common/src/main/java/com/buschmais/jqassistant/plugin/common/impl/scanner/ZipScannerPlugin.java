package com.buschmais.jqassistant.plugin.common.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.model.ArchiveDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;

public class ZipScannerPlugin extends AbstractArchiveScannerPlugin {

    @Override
    protected String getExtension() {
        return ".zip";
    }

    @Override
    protected Scope createScope(Scope currentScope) {
        return currentScope;
    }

    @Override
    protected ArchiveDescriptor createArchive(FileResource file, String path, ScannerContext scannerContext) {
        return scannerContext.getStore().create(ArchiveDescriptor.class);
    }
}
