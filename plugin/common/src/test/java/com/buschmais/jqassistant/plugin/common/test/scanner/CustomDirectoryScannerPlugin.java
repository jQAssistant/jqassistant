package com.buschmais.jqassistant.plugin.common.test.scanner;

import java.io.File;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.model.FileContainerDescriptor;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractDirectoryScannerPlugin;

public class CustomDirectoryScannerPlugin extends AbstractDirectoryScannerPlugin {

    @Override
    protected void initialize() {
    }

    @Override
    protected Scope getScope() {
        return CustomScope.CUSTOM;
    }

    @Override
    protected FileContainerDescriptor getContainerDescriptor(File container, ScannerContext scannerContext) {
        return scannerContext.getStore().create(CustomDirectoryDescriptor.class);
    }

}
