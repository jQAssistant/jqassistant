package com.buschmais.jqassistant.plugin.common.api.scanner.filesystem;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolverStrategy;

/**
 * Abstract base class for {@link FileResolverStrategy}s.
 * 
 * Provides utility functionality.
 */
public abstract class AbstractFileResolverStrategy implements FileResolverStrategy {

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
        } else {
            result = context.getStore().addDescriptorType(descriptor, type);
        }
        result.setFileName(path);
        return result;
    }
}
