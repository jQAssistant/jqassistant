package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.File;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.FileContainerDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractDirectoryScannerPlugin;

public class DirectoryScannerPlugin extends AbstractDirectoryScannerPlugin<FileContainerDescriptor> {

    @Override
    protected Scope getRequiredScope() {
        return DefaultScope.NONE;
    }

    @Override
    protected FileContainerDescriptor getContainerDescriptor(File container, ScannerContext scannerContext) {
        return scannerContext.getStore()
            .create(FileContainerDescriptor.class);
    }

    @Override
    protected void enterContainer(File directory, FileContainerDescriptor directoryDescriptor, ScannerContext scannerContext) {
    }

    @Override
    protected void leaveContainer(File directory, FileContainerDescriptor directoryDescriptor, ScannerContext scannerContext) {
    }
}
