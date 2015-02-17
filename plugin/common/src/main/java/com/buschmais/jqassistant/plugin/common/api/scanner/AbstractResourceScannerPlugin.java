package com.buschmais.jqassistant.plugin.common.api.scanner;

import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.DirectoryDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.DirectoryResource;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.Resource;

/**
 * Abstract base implementation for plugins handling file or directory
 * resources.
 * 
 * @param <I>
 *            The resource item type.
 * @param <D>
 *            The descriptor type representing the item type.
 */
public abstract class AbstractResourceScannerPlugin<I, D extends Descriptor> extends AbstractScannerPlugin<I, D> {

    @Override
    public Class<? extends I> getType() {
        return getTypeParameter(AbstractResourceScannerPlugin.class, 0);
    }

    @Override
    public Class<? extends D> getDescriptorType() {
        return getTypeParameter(AbstractResourceScannerPlugin.class, 1);
    }

    /**
     * Ensures that a file descriptor is returned.
     * 
     * @param descriptor
     *            The descriptor returned by the scanner.
     * @param relativePath
     *            The relativ path to be used for the file name attribute.
     * @param context
     *            The scanner context.
     * @param <F>
     *            The expected descriptor type.
     * @return The descriptor.
     * @throws IOException
     *             If the given descriptor does not represent a file.
     */
    protected <F extends FileDescriptor> F toFileDescriptor(Resource resource, Descriptor descriptor, String relativePath, ScannerContext context)
            throws IOException {
        FileDescriptor fileDescriptor;
        if (descriptor == null) {
            fileDescriptor = createFileDescriptor(resource, context);
        } else if (descriptor instanceof FileDescriptor) {
            fileDescriptor = (FileDescriptor) descriptor;
        } else {
            throw new IOException(descriptor + " must be extend from " + FileDescriptor.class);
        }
        fileDescriptor.setFileName(relativePath);
        return (F) fileDescriptor;
    }

    /**
     * Creates a file descriptor representing the given resource.
     * 
     * @param resource
     *            The resource.
     * @param context
     *            The scanner context.
     * @return The file descriptor.
     * @throws IOException
     *             If a resource of an unknown type is provided.
     */
    private FileDescriptor createFileDescriptor(Resource resource, ScannerContext context) throws IOException {
        Class<? extends FileDescriptor> type;
        if (resource instanceof DirectoryResource) {
            type = DirectoryDescriptor.class;
        } else if (resource instanceof FileResource) {
            type = FileDescriptor.class;
        } else {
            throw new IOException("Unsupported resource " + resource);
        }
        return context.getStore().create(type);
    }

}
