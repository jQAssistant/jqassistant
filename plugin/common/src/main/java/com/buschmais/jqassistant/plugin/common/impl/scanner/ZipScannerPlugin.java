package com.buschmais.jqassistant.plugin.common.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.ZipArchiveDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractZipArchiveScannerPlugin;

public class ZipScannerPlugin extends AbstractZipArchiveScannerPlugin<ZipArchiveDescriptor, ZipScannerPlugin> {

    @Override
    protected String getExtension() {
        return ".zip";
    }

    @Override
    protected Scope createScope(Scope currentScope, ZipArchiveDescriptor archiveDescriptor, ScannerContext context) {
        return currentScope;
    }

    @Override
    protected void destroyScope(ScannerContext scannerContext) {
    }

    @Override
    protected ZipScannerPlugin getThis() {
        return this;
    }
}
