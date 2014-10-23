package com.buschmais.jqassistant.plugin.common.api.scanner;

import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.core.store.api.model.FileDescriptor;

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
        return getType(AbstractResourceScannerPlugin.class, 0);
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
    protected <F extends FileDescriptor> F toFileDescriptor(Descriptor descriptor, String relativePath, ScannerContext context) throws IOException {
        FileDescriptor fileDescriptor;
        if (descriptor == null) {
            fileDescriptor = context.getStore().create(FileDescriptor.class);
        } else if (descriptor instanceof FileDescriptor) {
            fileDescriptor = (FileDescriptor) descriptor;
        } else {
            throw new IOException(descriptor + " must be extend from " + FileDescriptor.class);
        }
        fileDescriptor.setFileName(relativePath);
        return (F) fileDescriptor;
    }

}
