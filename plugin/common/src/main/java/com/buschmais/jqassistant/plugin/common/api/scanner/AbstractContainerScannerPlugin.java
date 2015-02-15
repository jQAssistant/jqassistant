package com.buschmais.jqassistant.plugin.common.api.scanner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.FileContainerDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.Resource;

/**
 * Abstract base implementation for scanner plugins that handle containers of
 * elements like directories, archives, etc.
 * 
 * @param <I>
 *            The container type.
 * @param <E>
 *            The element type.
 */
public abstract class AbstractContainerScannerPlugin<I, E, D extends FileContainerDescriptor> extends AbstractResourceScannerPlugin<I, D> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractContainerScannerPlugin.class);

    @Override
    public final D scan(I container, String path, Scope scope, Scanner scanner) throws IOException {
        ScannerContext context = scanner.getContext();
        D containerDescriptor = getContainerDescriptor(container, context);
        containerDescriptor.setFileName(path);
        LOGGER.info("Entering {}", path);
        Map<String, FileDescriptor> files = new HashMap<>();
        enterContainer(container, containerDescriptor, scanner.getContext());
        try {
            Iterable<? extends E> entries = getEntries(container);
            for (E e : entries) {
                Resource resource = getEntry(container, e);
                try {
                    String relativePath = getRelativePath(container, e);
                    LOGGER.info("Scanning {}", relativePath);
                    FileDescriptor descriptor = scanner.scan(resource, relativePath, scope);
                    descriptor = toFileDescriptor(resource, descriptor, relativePath, context);
                    files.put(relativePath, descriptor);
                    containerDescriptor.getContains().add(descriptor);
                } finally {
                    resource.close();
                }
            }
        } finally {
            leaveContainer(container, containerDescriptor, scanner.getContext());
            LOGGER.info("Leaving {}", path);
        }
        for (Map.Entry<String, FileDescriptor> entry : files.entrySet()) {
            String relativePath = entry.getKey();
            int separatorIndex = relativePath.lastIndexOf('/');
            if (separatorIndex != -1) {
                String parentName = relativePath.substring(0, separatorIndex);
                FileDescriptor fileDescriptor = files.get(parentName);
                if (fileDescriptor instanceof FileContainerDescriptor) {
                    ((FileContainerDescriptor) fileDescriptor).getContains().add(entry.getValue());
                }
            }
        }

        return containerDescriptor;
    }

    /**
     * Return the descriptor representing the artifact.
     * 
     * @param container
     *            The container.
     * @param scannerContext
     *            The scanner context.
     * @return The artifact descriptor.
     */
    protected abstract D getContainerDescriptor(I container, ScannerContext scannerContext);

    /**
     * Return an iterable which delivers the entries of the container.
     * <p>
     * The entries must not contain the relative root element, i.e. "/".
     * </p>
     * 
     * @param container
     *            The container.
     * @return The iterable of entries.
     * @throws IOException
     *             If the entries cannot be determined.
     */
    protected abstract Iterable<? extends E> getEntries(I container) throws IOException;

    /**
     * Return the relative path of an element within the container.
     * <p>
     * The following conditions must be considered:
     * <ul>
     * <li>The separator to use is "/".</li>
     * <li>The path must start with "/".</li>
     * <li>The path must not end with "/".</li>
     * </ul>
     *
     * </p>
     * 
     * @param container
     *            The container.
     * @param entry
     *            The entry.
     * @return The relative path.
     */
    protected abstract String getRelativePath(I container, E entry);

    /**
     * Create a scope depending on the container type, e.g. a JAR file should
     * return classpath scope.
     * 
     * @param container
     *            The container.
     * @param containerDescriptor
     *            The container descriptor.
     * @param scannerContext
     *            The scanner context.
     */
    protected abstract void enterContainer(I container, D containerDescriptor, ScannerContext scannerContext) throws IOException;

    /**
     * Destroy the container dependent scope.
     * 
     * @param container
     *            The container.
     * @param containerDescriptor
     *            The container descriptor
     * @param scannerContext
     *            The scanner context.
     */
    protected abstract void leaveContainer(I container, D containerDescriptor, ScannerContext scannerContext) throws IOException;

    /**
     * Return a {@link Resource} representing an entry.
     * 
     * @param container
     *            The container.
     * @param entry
     *            The entry.
     * @return The
     *         {@link com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource}
     *         .
     */
    protected abstract Resource getEntry(I container, E entry);

}
