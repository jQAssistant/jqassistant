package com.buschmais.jqassistant.plugin.javaee6.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractArchiveScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.javaee6.api.model.WebApplicationArchiveDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.scanner.WebApplicationScope;

public class WarArchiveScannerPlugin extends AbstractArchiveScannerPlugin<WebApplicationArchiveDescriptor> {

    @Override
    protected String getExtension() {
        return ".war";
    }

    @Override
    protected Scope createScope(Scope currentScope, WebApplicationArchiveDescriptor archiveDescriptor, ScannerContext scannerContext) {
        scannerContext.push(WebApplicationArchiveDescriptor.class, archiveDescriptor);
        return WebApplicationScope.WAR;
    }

    @Override
    protected void destroyScope(ScannerContext scannerContext) {
        scannerContext.pop(WebApplicationArchiveDescriptor.class);
    }

    @Override
    protected WebApplicationArchiveDescriptor createArchive(FileResource file, String path, ScannerContext scannerContext) {
        return scannerContext.getStore().create(WebApplicationArchiveDescriptor.class);
    }

}
