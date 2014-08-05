package com.buschmais.jqassistant.plugin.junit4.impl.scanner;

import java.io.File;
import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.type.DirectoryDescriptor;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractDirectoryScannerPlugin;
import com.buschmais.jqassistant.plugin.junit4.api.scanner.JunitScope;

public class TestReportDirectoryScannerPlugin extends AbstractDirectoryScannerPlugin<TestReportDirectory> {

    @Override
    protected File getDirectory(TestReportDirectory item) {
        return item.getDirectory();
    }

    @Override
    protected Scope createScope(Scope currentScope) {
        return JunitScope.TESTREPORTS;
    }

    @Override
    protected DirectoryDescriptor beforeDirectory(TestReportDirectory item, String path) {
        DirectoryDescriptor directoryDescriptor = getStore().create(DirectoryDescriptor.class);
        directoryDescriptor.setFileName(path);
        return directoryDescriptor;
    }

    @Override
    public Class<? super TestReportDirectory> getType() {
        return TestReportDirectory.class;
    }

    @Override
    public boolean accepts(TestReportDirectory item, String path, Scope scope) throws IOException {
        return true;
    }
}
