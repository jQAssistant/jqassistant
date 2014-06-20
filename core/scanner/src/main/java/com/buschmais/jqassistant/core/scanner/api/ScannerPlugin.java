package com.buschmais.jqassistant.core.scanner.api;

import java.io.IOException;
import java.util.Map;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;

/**
 * Defines the interface for a scanner plugin.
 * 
 * @param <I>
 *            The item type accepted by the plugin.
 */
public interface ScannerPlugin<I> {

    /**
     * Initialize the plugin.
     * 
     * @param store
     *            The {@link Store} instance to use.
     * @param properties
     *            The plugin properties.
     */
    void initialize(Store store, Map<String, Object> properties);

    /**
     * Return the item type accepted by the plugin.
     * 
     * @return The item type.
     */
    Class<? super I> getType();

    /**
     * Determine if the item is accepted by the plugin.
     * 
     * @param item
     *            The item.
     * @param path
     *            The path where the item is located.
     * @param scope
     *            The scope.
     * @return <code>true</code> if the plugin accepts the item.
     * @throws IOException
     *             If a problem occurs.
     */
    boolean accepts(I item, String path, Scope scope) throws IOException;

    /**
     * Scan the item.
     * 
     * @param item
     *            The item.
     * @param path
     *            The path where the item is located.
     * @param scope
     *            The scope.
     * @param scanner
     *            The scanner instance to delegate items this plugin resolves
     *            from the given item.
     * @return {@link com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor}
     *         instances representing the scanned item.
     * @throws IOException
     *             If a problem occurs.
     */
    Iterable<? extends FileDescriptor> scan(I item, String path, Scope scope, Scanner scanner) throws IOException;

}
