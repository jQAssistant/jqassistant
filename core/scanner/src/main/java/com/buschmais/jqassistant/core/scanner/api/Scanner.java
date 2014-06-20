package com.buschmais.jqassistant.core.scanner.api;

import java.io.IOException;

import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;

/**
 * Defines the scanner interface.
 */
public interface Scanner {

    /**
     * Scan the given item, e.g. a file, inputstream, directory, etc.
     * 
     * @param item
     *            The item.
     * @param scope
     *            The scope to passed to the plugins.
     * @param <I>
     *            The item type.
     * @return The
     *         {@link com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor}
     *         s returned by the plugins.
     * @throws IOException
     *             If scanning fails.
     */
    public <I> Iterable<? extends FileDescriptor> scan(I item, Scope scope) throws IOException;

    /**
     * Scan the given item, e.g. a file, inputstream, directory, etc.
     * 
     * @param item
     *            The item.
     * @param path
     *            The path to be passed to the plugins.
     * @param scope
     *            The scope to passed to the plugins.
     * @param <I>
     *            The item type.
     * @return The
     *         {@link com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor}
     *         s returned by the plugins.
     * @throws IOException
     *             If scanning fails.
     */
    public <I> Iterable<? extends FileDescriptor> scan(I item, String path, Scope scope) throws IOException;
}
