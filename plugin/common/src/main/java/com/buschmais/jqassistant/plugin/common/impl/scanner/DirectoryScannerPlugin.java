package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.File;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.model.DirectoryDescriptor;
import com.buschmais.jqassistant.core.store.api.model.FileContainerDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractDirectoryScannerPlugin;

public class DirectoryScannerPlugin extends AbstractDirectoryScannerPlugin<DirectoryDescriptor> {

    @Override
    protected Scope getRequiredScope() {
        return DefaultScope.NONE;
    }

    @Override
    protected DirectoryDescriptor getContainerDescriptor(File container, ScannerContext scannerContext) {
        return scannerContext.getStore().create(DirectoryDescriptor.class);
    }

    @Override
    protected void enterContainer(DirectoryDescriptor directoryDescriptor, ScannerContext scannerContext) {
    }

    @Override
    protected void leaveContainer(ScannerContext scannerContext) {
    }

}
