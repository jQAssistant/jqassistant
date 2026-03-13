package com.buschmais.jqassistant.plugin.common.api.scanner;

import java.util.function.Function;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;

/**
 * Abstract base class for {@link FileResolver}s.
 * <p>
 * Provides utility functionality.
 */
public abstract class AbstractFileResolver implements FileResolver {

    private final String cacheKey;

    /**
     * Constructor.
     *
     * @param cacheKey
     *            The cache key to use for the store.
     */
    protected AbstractFileResolver(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    @Override
    public <D extends FileDescriptor> D require(String requiredPath, Class<D> type, ScannerContext context) {
        return require(requiredPath, requiredPath, type, context);
    }

    /**
     * Takes an optional descriptor and transforms it to file descriptor.
     *
     * @param descriptor
     *            The descriptor, if <code>null</code> a new descriptor is created.
     * @param type
     *            The required type.
     * @param path
     *            The path (to set as file name).
     * @param context
     *            The scanner context.
     * @param <D>
     *            The required type.
     * @return The file descriptor.
     * @deprecated migrate to
     *             {@link #getOrCreateAs(String, Class, Function, ScannerContext)}.
     */
    @Deprecated
    protected <D extends FileDescriptor> D toFileDescriptor(Descriptor descriptor, Class<D> type, String path, ScannerContext context) {
        if (descriptor == null) {
            D result = context.getStore().create(type);
            result.setFileName(path);
            return result;
        }
        return migrateOrCast(descriptor, type, context);
    }

    /**
     * Get an existing {@link FileDescriptor} or create a new one. If an existing
     * {@link FileDescriptor} exists it will be migrated on demand.
     *
     * @param path
     *            The path.
     * @param type
     *            The requested type.
     * @param resolveExisting
     *            A {@link Function} to resolve an existing {@link FileDescriptor}.
     * @param context
     *            The {@link ScannerContext}.
     * @param <D>
     *            The requested type.
     * @return The {@link FileDescriptor}.
     */
    protected <D extends FileDescriptor> D getOrCreateAs(String path, Class<D> type, Function<String, FileDescriptor> resolveExisting, ScannerContext context) {
        FileDescriptor descriptor = context.getStore().<String, FileDescriptor> getCache(cacheKey).get(path, p -> {
            FileDescriptor fileDescriptor = resolveExisting.apply(p);
            if (fileDescriptor != null) {
                return fileDescriptor;
            }
            fileDescriptor = context.getStore().create(type);
            fileDescriptor.setFileName(path);
            return fileDescriptor;
        });
        return migrateOrCast(descriptor, type, context);
    }

    /**
     * Ensures if the given {@link FileDescriptor} implements the requested type by
     * migrating or just casting it.
     *
     * @param descriptor
     *            The {@link FileDescriptor}.
     * @param type
     *            The requested type.
     * @param context
     *            The {@link ScannerContext}.
     * @param <D>
     *            The requested type.
     * @return The {@link FileDescriptor} that implements the requested type.
     */
    private <D extends FileDescriptor> D migrateOrCast(Descriptor descriptor, Class<D> type, ScannerContext context) {
        return type.isAssignableFrom(descriptor.getClass()) ? type.cast(descriptor) : context.getStore().addDescriptorType(descriptor, type);
    }

}
