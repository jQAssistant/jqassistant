package com.buschmais.jqassistant.core.scanner.impl;

import static java.util.Collections.emptyList;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.scanner.api.iterable.AggregatingIterable;
import com.buschmais.jqassistant.core.scanner.api.iterable.MappingIterable;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;

public class ScannerImpl implements Scanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScannerImpl.class);

    private final List<ScannerPlugin<?>> scannerPlugins;

    public ScannerImpl(List<ScannerPlugin<?>> scannerPlugins) {
        this.scannerPlugins = scannerPlugins;
    }

    @Override
    public <I> Iterable<? extends FileDescriptor> scan(I item, Scope scope) throws IOException {
        return scan(item, null, scope);
    }

    @Override
    public <I> Iterable<? extends FileDescriptor> scan(final I item, final String relativePath, final Scope scope) throws IOException {
        MappingIterable<ScannerPlugin<?>, Iterable<? extends FileDescriptor>> fileDescriptors = new MappingIterable<ScannerPlugin<?>, Iterable<? extends FileDescriptor>>(
                scannerPlugins) {
            @Override
            protected Iterable<? extends FileDescriptor> map(ScannerPlugin<?> scannerPlugin) throws IOException {
                Class<?> scannerPluginType = scannerPlugin.getType();
                if (scannerPluginType.isAssignableFrom(item.getClass())) {
                    ScannerPlugin<I> selectedPlugin = (ScannerPlugin<I>) scannerPlugin;
                    if (selectedPlugin.accepts(item, relativePath, scope)) {
                        return selectedPlugin.scan(item, relativePath, scope, ScannerImpl.this);
                    }
                }
                return emptyList();
            }
        };

        return new AggregatingIterable<>(fileDescriptors);
    }
}
