package com.buschmais.jqassistant.core.scanner.impl;

import java.io.IOException;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.Scope;

/**
 * Test plugin: creates a TestDescriptor1.
 */
public class TestScannerPlugin1 implements ScannerPlugin<TestItem, TestDescriptor1> {

    @Override
    public void initialize() {
    }

    @Override
    public void configure(ScannerContext scannerContext, Map<String, Object> properties) {
    }

    @Override
    public Class<? extends TestItem> getType() {
        return TestItem.class;
    }

    @Override
    public Class<TestDescriptor1> getDescriptorType() {
        return TestDescriptor1.class;
    }

    @Override
    public boolean accepts(TestItem item, String path, Scope scope) throws IOException {
        return true;
    }

    @Override
    public TestDescriptor1 scan(TestItem item, String path, Scope scope, Scanner scanner) throws IOException {
        return scanner.getContext().getStore().create(TestDescriptor1.class);
    }
}
