package com.buschmais.jqassistant.plugin.common.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.AbstractFileResolver;

/**
 * Default implementation of a file resolver.
 * 
 * Always create a new file descriptor.
 */
public class DefaultFileResolver extends AbstractFileResolver {

    @Override
    public <D extends FileDescriptor> D require(String path, Class<D> type, ScannerContext context) {
        return toFileDescriptor(null, type, path, context);
    }

    @Override
    public <D extends FileDescriptor> D match(String path, Class<D> type, ScannerContext context) {
        return toFileDescriptor(null, type, path, context);
    }
}
