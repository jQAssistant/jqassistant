package com.buschmais.jqassistant.plugin.junit4.impl.scanner;

import java.io.File;
import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
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
    protected void beforeDirectory(TestReportDirectory item, String path) {
    }

    @Override
    protected Iterable<? extends FileDescriptor> afterDirectory(TestReportDirectory item, Iterable<? extends FileDescriptor> descriptors) {
        return descriptors;
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
