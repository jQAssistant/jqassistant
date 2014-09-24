package com.buschmais.jqassistant.core.scanner.api;

import java.io.IOException;
import java.util.Map;

import com.buschmais.jqassistant.core.store.api.type.FileDescriptor;

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
     * @param properties
     *            The plugin properties.
     */
    void initialize(Map<String, Object> properties);

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
     * @return The {@link FileDescriptor} instance representing the scanned
     *         item.
     * @throws IOException
     *             If a problem occurs.
     */
    FileDescriptor scan(I item, String path, Scope scope, Scanner scanner) throws IOException;

}
