package com.buschmais.jqassistant.core.scanner.impl;

import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.Scope;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Test plugin: migrates a required {@link TestItemDescriptor} to a {@link NestedTestItemDescriptor}.
 */
@ScannerPlugin.Requires(DependentTestItemDescriptor.class)
public abstract class NestedTestItemScannerPlugin implements ScannerPlugin<TestItem, NestedTestItemDescriptor> {

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
    public NestedTestItemDescriptor scan(TestItem item, String path, Scope scope, Scanner scanner) {
        assertThat((DependentTestItemDescriptor) scanner.getContext()
            .getCurrentDescriptor()).isInstanceOf(DependentTestItemDescriptor.class);
        return mock(NestedTestItemDescriptor.class);
    }
}
