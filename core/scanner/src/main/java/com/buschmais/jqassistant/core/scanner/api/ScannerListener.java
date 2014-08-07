package com.buschmais.jqassistant.core.scanner.api;

import com.buschmais.jqassistant.core.store.api.type.FileDescriptor;

/**
 * Defines a listener interface for the scanner.
 */
public interface ScannerListener {

    /**
     * Callback which is invoked before an item is scanned.
     * 
     * @param item
     *            The item.
     * @param relativePath
     *            The path.
     * @param scope
     *            The scope
     * @param <I>
     *            The item type.
     */
    <I> void before(I item, String relativePath, Scope scope);

    /**
     * Callback which is invoked after an item has been scanned.
     * 
     * @param item
     *            The item.
     * @param relativePath
     *            The path.
     * @param scope
     *            The scope
     * @param fileDescriptor
     *            The created file descriptor.
     * @param <I>
     *            The item type.
     */
    <I> void after(I item, String relativePath, Scope scope, FileDescriptor fileDescriptor);
}
