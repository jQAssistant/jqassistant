package com.buschmais.jqassistant.core.scanner.impl;

import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.Scope;

/**
 * Test plugin: migrates a required {@link TestItemDescriptor} to a {@link NestedTestItemDescriptor}.
 */
public class NestedTestItemScannerPlugin implements ScannerPlugin<TestItem, NestedTestItemDescriptor> {

    @Override
    public void initialize() {
    }

    @Override
    public void configure(ScannerContext scannerContext, Map<String, Object> properties) {
    }

    @Override
    public String getName() {
        return NestedTestItemScannerPlugin.class.getSimpleName();
    }

    @Override
    public Class<? extends TestItem> getType() {
        return TestItem.class;
    }

    @Override
    public Class<NestedTestItemDescriptor> getDescriptorType() {
        return NestedTestItemDescriptor.class;
    }

    @Override
    public boolean accepts(TestItem item, String path, Scope scope) {
        return TestScope.TEST.equals(scope);
    }

    @Override
    public NestedTestItemDescriptor scan(TestItem item, String path, Scope scope, Scanner scanner) {
        TestItemDescriptor testItemDescriptor = scanner.getContext().getCurrentDescriptor();
        return scanner.getContext().getStore().addDescriptorType(testItemDescriptor, NestedTestItemDescriptor.class);
    }
}
