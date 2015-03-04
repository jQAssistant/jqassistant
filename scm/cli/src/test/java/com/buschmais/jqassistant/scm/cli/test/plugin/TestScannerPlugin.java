package com.buschmais.jqassistant.scm.cli.test.plugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;

public class TestScannerPlugin extends AbstractScannerPlugin<File,FileDescriptor> {

    @Override
    public boolean accepts(File item, String path, Scope scope) throws IOException {
        return false;
    }

    @Override
    public FileDescriptor scan(File item, String path, Scope scope, Scanner scanner) throws IOException {
        return null;
    }

    public Map<String, Object> getProperties() {
        return super.getProperties();
    }

}
