package com.buschmais.jqassistant.plugin.junit.impl.scanner;

import java.io.File;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.model.DirectoryDescriptor;
import com.buschmais.jqassistant.core.store.api.model.FileContainerDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractDirectoryScannerPlugin;
import com.buschmais.jqassistant.plugin.junit.api.model.TestReportDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.junit.api.scanner.JunitScope;

public class TestReportDirectoryScannerPlugin extends AbstractDirectoryScannerPlugin<TestReportDirectoryDescriptor> {

    @Override
    protected Scope getRequiredScope() {
        return JunitScope.TESTREPORTS;
    }

    @Override
    protected TestReportDirectoryDescriptor getContainerDescriptor(File container, ScannerContext scannerContext) {
        return scannerContext.getStore().create(TestReportDirectoryDescriptor.class);
    }

}
