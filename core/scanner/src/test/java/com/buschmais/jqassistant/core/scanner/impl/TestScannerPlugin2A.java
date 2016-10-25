package com.buschmais.jqassistant.core.scanner.impl;

import java.io.IOException;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.Scope;

/**
 * Test plugin: migrates a required TestDescriptor1 to a TestDescriptor2.
 */
public class TestScannerPlugin2A implements ScannerPlugin<TestItem, TestDescriptor2A> {

    @Override
    public void initialize() {
    }

    @Override
    public void configure(ScannerContext scannerContext, Map<String, Object> properties) {
    }

    @Override
    public String getName() {
        return TestScannerPlugin2A.class.getSimpleName();
    }

    @Override
    public Class<? extends TestItem> getType() {
        return TestItem.class;
    }

    @Override
    public Class<TestDescriptor2A> getDescriptorType() {
        return TestDescriptor2A.class;
    }

    @Override
    public boolean accepts(TestItem item, String path, Scope scope) throws IOException {
        return TestScope.TEST.equals(scope);
    }

    @Override
    public TestDescriptor2A scan(TestItem item, String path, Scope scope, Scanner scanner) throws IOException {
        TestDescriptor1 testDescriptor1 = scanner.getContext().peek(TestDescriptor1.class);
        return scanner.getContext().getStore().addDescriptorType(testDescriptor1, TestDescriptor2A.class);
    }
}
