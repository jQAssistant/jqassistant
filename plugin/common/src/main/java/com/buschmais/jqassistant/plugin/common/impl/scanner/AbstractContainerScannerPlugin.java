package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.type.FileContainerDescriptor;
import com.buschmais.jqassistant.core.store.api.type.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileSystemResource;

/**
 * Abstract base implementation for scanner plugins that handle containers of
 * elements like directories, archives, etc.
 * 
 * @param <I>
 *            The container type.
 * @param <E>
 *            The element type.
 */
public abstract class AbstractContainerScannerPlugin<I, E> extends AbstractScannerPlugin<I> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractContainerScannerPlugin.class);

    @Override
    public final FileDescriptor scan(I container, String path, Scope scope, Scanner scanner) throws IOException {
        FileContainerDescriptor containerDescriptor = getContainerDescriptor(container);
        containerDescriptor.setFileName(path);
        for (E e : getEntries(container)) {
            FileSystemResource fileSystemResource = getFileResource(container, e);
            String relativePath = getRelativePath(container, e);
            Scope entryScope = createScope(scope);
            LOGGER.info("Scanning '{}'.", relativePath);
            FileDescriptor descriptor = scanner.scan(fileSystemResource, relativePath, entryScope);
            if (descriptor == null) {
                descriptor = getStore().create(FileDescriptor.class);
            }
            descriptor.setFileName(relativePath);
            containerDescriptor.getContains().add(descriptor);
        }
        return containerDescriptor;
    }

    /**
     * Return the descriptor representing the container.
     * 
     * @param container
     *            The container.
     * @return The descriptor.
     */
    protected abstract FileContainerDescriptor getContainerDescriptor(I container);

    /**
     * Return an iterable which delivers the entries of the container.
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
     * It should start with "/".
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
     * @param currentScope
     *            The current scope.
     * @return The scope.
     */
    protected abstract Scope createScope(Scope currentScope);

    /**
     * Return a {@link FileSystemResource} representing an entry.
     * 
     * @param container
     *            The container.
     * @param entry
     *            The entry.
     * @return The {@link FileSystemResource}.
     */
    protected abstract FileSystemResource getFileResource(I container, E entry);

}
