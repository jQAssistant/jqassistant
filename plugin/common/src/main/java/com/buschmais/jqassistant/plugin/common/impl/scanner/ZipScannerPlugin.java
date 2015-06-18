package com.buschmais.jqassistant.plugin.common.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.ArchiveDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractArchiveScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;

public class ZipScannerPlugin extends AbstractArchiveScannerPlugin<ArchiveDescriptor> {

    @Override
    protected String getExtension() {
        return ".zip";
    }

    @Override
    protected Scope createScope(Scope currentScope, ArchiveDescriptor archiveDescriptor, ScannerContext context) {
        return currentScope;
    }

    @Override
    protected void destroyScope(ScannerContext scannerContext) {
    }

    @Override
    protected ArchiveDescriptor createArchive(FileResource file, String path, ScannerContext scannerContext) {
        return scannerContext.getStore().addDescriptorType(scannerContext.peek(FileDescriptor.class), ArchiveDescriptor.class);
    }
}
