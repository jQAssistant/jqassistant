package com.buschmais.jqassistant.plugin.common.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;

/**
 * Default implementation of a file resolver.
 * 
 * Always create a new file descriptor.
 */
public class DefaultFileResolver extends AbstractFileResolver {

    @Override
    public <D extends FileDescriptor> D require(String requiredPath, String containedPath, Class<D> type, ScannerContext context) {
        return toFileDescriptor(null, type, requiredPath, context);
    }

    @Override
    public <D extends FileDescriptor> D match(String containedPath, Class<D> type, ScannerContext context) {
        return toFileDescriptor(null, type, containedPath, context);
    }
}
