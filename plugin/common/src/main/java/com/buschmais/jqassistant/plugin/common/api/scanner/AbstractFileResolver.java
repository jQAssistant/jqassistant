package com.buschmais.jqassistant.plugin.common.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;

/**
 * Abstract base class for {@link FileResolver}s.
 * 
 * Provides utility functionality.
 */
public abstract class AbstractFileResolver implements FileResolver {

    @Override
    public <D extends FileDescriptor> D require(String requiredPath, Class<D> type, ScannerContext context) {
        return require(requiredPath, requiredPath, type, context);
    }

    /**
     * Takes an optional descriptor and transforms it to file descriptor.
     * 
     * @param descriptor
     *            The descriptor, if <code>null</code> a new descriptor is
     *            created.
     * @param type
     *            The required type.
     * @param path
     *            The path (to set as file name).
     * @param context
     *            The scanner context.
     * @param <D>
     *            The required type.
     * @return The file descriptor.
     */
    protected <D extends FileDescriptor> D toFileDescriptor(Descriptor descriptor, Class<D> type, String path, ScannerContext context) {
        D result;
        if (descriptor == null) {
            result = context.getStore().create(type);
            result.setFileName(path);
        } else if (type.isAssignableFrom(descriptor.getClass())) {
            result = type.cast(descriptor);
        } else {
            result = context.getStore().addDescriptorType(descriptor, type);
            result.setFileName(path);
        }
        return result;
    }
}
