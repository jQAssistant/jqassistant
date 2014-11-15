package com.buschmais.jqassistant.plugin.javaee6.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.model.ArchiveDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractArchiveScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.javaee6.api.model.EarArchiveDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.scanner.EnterpriseApplicationScope;

public class EarArchiveScannerPlugin extends AbstractArchiveScannerPlugin {

    @Override
    protected String getExtension() {
        return ".ear";
    }

    @Override
    protected Scope createScope(Scope currentScope, ArchiveDescriptor archive, ScannerContext context) {
        return EnterpriseApplicationScope.EAR;
    }

    @Override
    protected void destroyScope(ScannerContext scannerContext) {
    }

    @Override
    protected ArchiveDescriptor createArchive(FileResource file, String path, ScannerContext scannerContext) {
        return scannerContext.getStore().create(EarArchiveDescriptor.class);
    }
}
