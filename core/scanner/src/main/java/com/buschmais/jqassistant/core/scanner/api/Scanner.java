package com.buschmais.jqassistant.core.scanner.api;

import java.io.IOException;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.core.store.api.model.FileDescriptor;

/**
 * Defines the scanner interface.
 */
public interface Scanner {

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
     * @param <D>
     *            The corresponding descriptor type.
     * @return The {@link FileDescriptor} as returned by the plugins.
     * @throws IOException
     *             If scanning fails.
     */
    public <I, D extends Descriptor> D scan(I item, String path, Scope scope);

    /**
     * Return an instance of the scanner context.
     * 
     * @return The scanner context.
     */
    ScannerContext getContext();
}
