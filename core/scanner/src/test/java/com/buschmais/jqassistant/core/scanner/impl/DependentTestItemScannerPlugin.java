package com.buschmais.jqassistant.core.scanner.impl;

import java.io.IOException;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.core.scanner.api.Scope;

/**
 * Test plugin: delegates scanning of the item using a custom scope before
 * migrating the returned descriptor to TestDescriptor2.
 */
@Requires(TestItemDescriptor.class)
public class DependentTestItemScannerPlugin implements ScannerPlugin<TestItem, DependentTestItemDescriptor> {

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
    public Class<DependentTestItemDescriptor> getDescriptorType() {
        return DependentTestItemDescriptor.class;
    }

    @Override
    public boolean accepts(TestItem item, String path, Scope scope) throws IOException {
        return true;
    }

    @Override
    public DependentTestItemDescriptor scan(TestItem item, String path, Scope scope, Scanner scanner) {
        NestedTestItemDescriptor nestedTestItemDescriptor = scanner.scan(item, path, TestScope.TEST);
        return scanner.getContext().getStore().addDescriptorType(nestedTestItemDescriptor, DependentTestItemDescriptor.class);
    }

    @Override
    public String getName() {
        return DependentTestItemScannerPlugin.class.getSimpleName();
    }
}
