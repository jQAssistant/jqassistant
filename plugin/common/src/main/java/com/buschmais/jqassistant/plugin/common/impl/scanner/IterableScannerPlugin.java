package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.scanner.api.iterable.AggregatingIterable;
import com.buschmais.jqassistant.core.scanner.api.iterable.MappingIterable;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;

public class IterableScannerPlugin<T> extends AbstractScannerPlugin<Iterable<T>> {
    @Override
    protected void initialize() {
    }

    @Override
    public Class<? super Iterable<T>> getType() {
        return Iterable.class;
    }

    @Override
    public boolean accepts(Iterable<T> item, String path, Scope scope) throws IOException {
        return true;
    }

    @Override
    public Iterable<? extends FileDescriptor> scan(Iterable<T> iterable, final String path, final Scope scope, final Scanner scanner) throws IOException {
        return new AggregatingIterable<>(new MappingIterable<T, Iterable<? extends FileDescriptor>>(iterable) {
            @Override
            protected Iterable<? extends FileDescriptor> map(T element) throws IOException {
                return scanner.scan(element, path, scope);
            }
        });
    }
}
