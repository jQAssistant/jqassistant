package com.buschmais.jqassistant.plugin.javaee6.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.model.ArchiveDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractArchiveScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.javaee6.api.model.WarArchiveDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.scanner.WebApplicationScope;

public class WarArchiveScannerPlugin extends AbstractArchiveScannerPlugin {

    @Override
    protected String getExtension() {
        return ".war";
    }

    @Override
    protected Scope createScope(Scope currentScope) {
        return WebApplicationScope.WAR;
    }

    @Override
    protected ArchiveDescriptor createArchive(FileResource file, String path, ScannerContext scannerContext) {
        return scannerContext.getStore().create(WarArchiveDescriptor.class);
    }
}
