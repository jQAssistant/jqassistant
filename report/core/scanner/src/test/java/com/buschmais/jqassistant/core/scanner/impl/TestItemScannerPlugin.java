package com.buschmais.jqassistant.core.scanner.impl;

import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.Scope;

import static org.mockito.Mockito.mock;

/**
 * Test plugin: creates a TestDescriptor1.
 */
public abstract class TestItemScannerPlugin implements ScannerPlugin<TestItem, TestItemDescriptor> {

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
    public Class<TestItemDescriptor> getDescriptorType() {
        return TestItemDescriptor.class;
    }

    @Override
    public TestItemDescriptor scan(TestItem item, String path, Scope scope, Scanner scanner) {
        return mock(TestItemDescriptor.class);
    }

    @Override
    public String getName() {
        return TestItemScannerPlugin.class.getSimpleName();
    }
}
