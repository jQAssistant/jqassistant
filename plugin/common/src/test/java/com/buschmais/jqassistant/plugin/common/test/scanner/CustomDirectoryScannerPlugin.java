package com.buschmais.jqassistant.plugin.common.test.scanner;

import java.io.File;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.model.FileContainerDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractDirectoryScannerPlugin;

public class CustomDirectoryScannerPlugin extends AbstractDirectoryScannerPlugin<CustomDirectoryDescriptor> {

    @Override
    protected Scope getRequiredScope() {
        return CustomScope.CUSTOM;
    }

    @Override
    protected CustomDirectoryDescriptor getContainerDescriptor(File container, ScannerContext scannerContext) {
        return scannerContext.getStore().create(CustomDirectoryDescriptor.class);
    }

}
