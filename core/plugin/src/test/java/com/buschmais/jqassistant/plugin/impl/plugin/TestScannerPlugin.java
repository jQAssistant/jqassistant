package com.buschmais.jqassistant.plugin.impl.plugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;

public class TestScannerPlugin implements ScannerPlugin<File, Descriptor> {

    private Map<String, Object> properties;

    @Override
    public void initialize() {
    }

    @Override
    public void configure(ScannerContext scannerContext, Map<String, Object> properties) {
        this.properties = properties;
    }

    @Override
    public Class<? extends File> getType() {
        return File.class;
    }

    @Override
    public Class<? extends Descriptor> getDescriptorType() {
        return Descriptor.class;
    }

    @Override
    public boolean accepts(File item, String path, Scope scope) throws IOException {
        return false;
    }

    @Override
    public Descriptor scan(File item, String path, Scope scope, Scanner scanner) throws IOException {
        return null;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

}
