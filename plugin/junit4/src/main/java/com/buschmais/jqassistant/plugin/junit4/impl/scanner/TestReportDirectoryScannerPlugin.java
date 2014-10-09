package com.buschmais.jqassistant.plugin.junit4.impl.scanner;

import java.io.File;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.type.DirectoryDescriptor;
import com.buschmais.jqassistant.core.store.api.type.FileContainerDescriptor;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractDirectoryScannerPlugin;
import com.buschmais.jqassistant.plugin.junit4.api.scanner.JunitScope;

public class TestReportDirectoryScannerPlugin extends AbstractDirectoryScannerPlugin {

    @Override
    protected Scope getScope() {
        return JunitScope.TESTREPORTS;
    }

    @Override
    protected FileContainerDescriptor getContainerDescriptor(File container, ScannerContext scannerContext) {
        return scannerContext.getStore().create(DirectoryDescriptor.class);
    }

}
