package com.buschmais.jqassistant.plugin.common.api.scanner;

import java.util.function.Function;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Abstract base class for {@link FileResolver}s.
 * <p>
 * Provides utility functionality.
 */
public abstract class AbstractFileResolver implements FileResolver {

    private Cache<String, FileDescriptor> cache = Caffeine.newBuilder().softValues().build();

    @Override
    public <D extends FileDescriptor> D require(String requiredPath, Class<D> type, ScannerContext context) {
        return require(requiredPath, requiredPath, type, context);
    }

    /**
     * Takes an optional descriptor and transforms it to file descriptor.
     *
     * @param descriptor The descriptor, if <code>null</code> a new descriptor is
     *                   created.
     * @param type       The required type.
     * @param path       The path (to set as file name).
     * @param context    The scanner context.
     * @param <D>        The required type.
     * @return The file descriptor.
     * @deprecated migrate to {@link #getOrCreateAs(String, Class, Function, ScannerContext)}.
     */
    @Deprecated
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

    /**
     * Get an existing {@link FileDescriptor} or create a new one. If an existing {@link FileDescriptor} exists it will be migrated on demand.
     *
     * @param path     The path.
     * @param type     The requested type.
     * @param existing A {@link Function} to resolve an existing {@link FileDescriptor}.
     * @param context  The {@link ScannerContext}.
     * @param <D>      The requested type.
     * @return The {@link FileDescriptor}.
     */
    protected <D extends FileDescriptor> D getOrCreateAs(String path, Class<D> type, Function<String, FileDescriptor> existing, ScannerContext context) {
        FileDescriptor descriptor = cache.get(path, p -> {
            FileDescriptor fileDescriptor = existing.apply(p);
            if (fileDescriptor != null) {
                return fileDescriptor;
            }
            fileDescriptor = context.getStore().create(type);
            fileDescriptor.setFileName(path);
            return fileDescriptor;
        });
        if (type.isAssignableFrom(descriptor.getClass())) {
            return type.cast(descriptor);
        } else {
            return context.getStore().addDescriptorType(descriptor, type);
        }
    }
}
