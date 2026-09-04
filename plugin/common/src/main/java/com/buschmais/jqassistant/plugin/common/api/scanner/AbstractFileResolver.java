package com.buschmais.jqassistant.plugin.common.api.scanner;

import java.util.function.Function;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;

import lombok.RequiredArgsConstructor;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Abstract base class for {@link FileResolver}s.
 * <p>
 * Provides utility functionality.
 */
@RequiredArgsConstructor
public abstract class AbstractFileResolver implements FileResolver {

    private final String path;

    private final String cacheKey;

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public <D extends FileDescriptor> D require(String requiredFileName, Class<D> type, ScannerContext context) {
        return require(requiredFileName, requiredFileName, type, context);
    }

    /**
     * Get an existing {@link FileDescriptor} or create a new one. If an existing
     * {@link FileDescriptor} exists it will be migrated on demand.
     *
     * @param fileName
     *     The required file name.
     * @param fileDescriptorType
     *     The requested {@link FileDescriptor} type.
     * @param resolveExisting
     *     A {@link Function} to resolve an existing {@link FileDescriptor}.
     * @param isMatch
     *     if <code>true</code> set the path attribute to indicate an existing file (it has been matched by a scanner/resolver)
     * @param context
     *     The {@link ScannerContext}.
     * @param <D>
     *     The requested type.
     * @return The {@link FileDescriptor}.
     */
    protected <D extends FileDescriptor> D getOrCreateAs(String fileName, Class<D> fileDescriptorType, Function<String, FileDescriptor> resolveExisting,
        boolean isMatch, ScannerContext context) {
        FileDescriptor descriptor = context.getStore()
            .<String, FileDescriptor>getCache(cacheKey)
            .get(fileName, f -> {
                FileDescriptor fileDescriptor = resolveExisting.apply(f);
                if (fileDescriptor != null) {
                    return fileDescriptor;
                }
                fileDescriptor = context.getStore()
                    .create(fileDescriptorType);
                // TODO fileName currently starts with a leading "/", to be changed in 3.x
                fileDescriptor.setFileName(fileName);
                return fileDescriptor;
            });
        if (isMatch) {
            String relativePath = fileName.substring(1);
            descriptor.setPath(isNotEmpty(path) ? path + "/" + relativePath : relativePath);
        }
        return migrateOrCast(descriptor, fileDescriptorType, context);
    }

    /**
     * Ensures if the given {@link FileDescriptor} implements the requested type by
     * migrating or just casting it.
     *
     * @param descriptor
     *     The {@link FileDescriptor}.
     * @param type
     *     The requested type.
     * @param context
     *     The {@link ScannerContext}.
     * @param <D>
     *     The requested type.
     * @return The {@link FileDescriptor} that implements the requested type.
     */
    private <D extends FileDescriptor> D migrateOrCast(Descriptor descriptor, Class<D> type, ScannerContext context) {
        return type.isAssignableFrom(descriptor.getClass()) ?
            type.cast(descriptor) :
            context.getStore()
                .addDescriptorType(descriptor, type);
    }

}
