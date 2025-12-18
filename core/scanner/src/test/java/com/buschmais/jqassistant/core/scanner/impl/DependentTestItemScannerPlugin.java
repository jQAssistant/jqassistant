package com.buschmais.jqassistant.core.scanner.impl;

import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.core.scanner.api.Scope;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Test plugin: delegates scanning of the item using a custom scope before
 * migrating the returned descriptor to TestDescriptor2.
 */
@Requires(TestItemDescriptor.class)
public abstract class DependentTestItemScannerPlugin implements ScannerPlugin<TestItem, DependentTestItemDescriptor> {

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
    public DependentTestItemDescriptor scan(TestItem item, String path, Scope scope, Scanner scanner) {
        assertThat((TestItemDescriptor) scanner.getContext()
            .getCurrentDescriptor()).isInstanceOf(TestItemDescriptor.class);
        return mock(DependentTestItemDescriptor.class);
    }

    @Override
    public String getName() {
        return DependentTestItemScannerPlugin.class.getSimpleName();
    }
}
