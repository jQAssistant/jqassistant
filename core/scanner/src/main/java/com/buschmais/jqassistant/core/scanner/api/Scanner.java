package com.buschmais.jqassistant.core.scanner.api;

import java.io.IOException;

import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;

public interface Scanner {

    public <I> Iterable<? extends FileDescriptor> scan(I item, Scope scope) throws IOException;

    public <I> Iterable<? extends FileDescriptor> scan(I item, String relativePath, Scope scope) throws IOException;
}
