package com.buschmais.jqassistant.plugin.common.api.scanner;

import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.shared.annotation.ToBeRemovedInVersion;
import com.buschmais.jqassistant.plugin.common.api.model.DirectoryDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.Resource;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Abstract base implementation for scanner plugins that handle containers of
 * elements like directories, archives, etc.
 *
 * @param <I>
 *     The container type.
 * @param <E>
 *     The element type.
 * @param <D>
 *     The descriptor type.
 */
public abstract class AbstractContainerScannerPlugin<I, E, D extends DirectoryDescriptor> extends AbstractResourceScannerPlugin<I, D> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractContainerScannerPlugin.class);

    @Override
    public final D scan(I container, String relativeContainerPath, Scope scope, Scanner scanner) throws IOException {
        ScannerContext context = scanner.getContext();
        FileResolver parentFileResolver = context.peek(FileResolver.class);
        D containerDescriptor = parentFileResolver.match(relativeContainerPath, getDescriptorType(), context);
        String containerPath = containerDescriptor.getPath();
        LOGGER.info("Entering {}", containerPath);
        ContainerFileResolver fileResolver = new ContainerFileResolver(containerPath, context, containerDescriptor);
        context.push(FileResolver.class, fileResolver);
        enterContainer(container, containerDescriptor, scanner.getContext());
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            Iterable<? extends E> entries = getEntries(container);
            for (E entry : entries) {
                String relativeEntryPath = getRelativePath(container, entry);
                try (Resource resource = getEntry(container, entry)) {
                    LOGGER.debug("Scanning {}", relativeEntryPath);
                    FileDescriptor descriptor = scanner.scan(resource, relativeEntryPath, scope);
                    if (descriptor != null) {
                        fileResolver.put(relativeEntryPath, descriptor);
                    }
                }
            }
        } finally {
            leaveContainer(container, containerDescriptor, scanner.getContext());
            context.pop(FileResolver.class);
        }
        fileResolver.flush();
        LOGGER.info("Leaving {} ({} entries, {} ms)", containerPath, fileResolver.size(), stopwatch.elapsed(MILLISECONDS));
        return containerDescriptor;
    }

    /**
     * Return the descriptor representing the artifact.
     *
     * @param container
     *     The container.
     * @param scannerContext
     *     The scanner context.
     * @return The artifact descriptor.
     *
     * @deprecated This method is no longer invoked.
     */
    @Deprecated
    @ToBeRemovedInVersion(major = 3, minor = 0)
    protected abstract D getContainerDescriptor(I container, ScannerContext scannerContext);

    /**
     * Return an iterable which delivers the entries of the container.
     * <p>
     * The entries must not contain the relative root element, i.e. "/".
     * </p>
     *
     * @param container
     *     The container.
     * @return The iterable of entries.
     * @throws IOException
     *     If the entries cannot be determined.
     */
    protected abstract Iterable<? extends E> getEntries(I container) throws IOException;

    /**
     * Return the normalized path to the container.
     *
     * @param container
     *     The container.
     * @param path
     *     The provided path.
     * @return The normalized path.
     */
    @ToBeRemovedInVersion(major = 3, minor = 0)
    @Deprecated
    protected String getContainerPath(I container, String path) {
        return path;
    }

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
     *     The container.
     * @param entry
     *     The entry.
     * @return The relative path.
     */
    protected abstract String getRelativePath(I container, E entry);

    /**
     * Create a scope depending on the container type, e.g. a JAR file should
     * return classpath scope.
     *
     * @param container
     *     The container.
     * @param containerDescriptor
     *     The container descriptor.
     * @param scannerContext
     *     The scanner context.
     */
    protected abstract void enterContainer(I container, D containerDescriptor, ScannerContext scannerContext) throws IOException;

    /**
     * Destroy the container dependent scope.
     *
     * @param container
     *     The container.
     * @param containerDescriptor
     *     The container descriptor
     * @param scannerContext
     *     The scanner context.
     */
    protected abstract void leaveContainer(I container, D containerDescriptor, ScannerContext scannerContext) throws IOException;

    /**
     * Return a {@link Resource} representing an entry.
     *
     * @param container
     *     The container.
     * @param entry
     *     The entry.
     * @return The
     * {@link com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource}
     * .
     */
    protected abstract Resource getEntry(I container, E entry);

}
