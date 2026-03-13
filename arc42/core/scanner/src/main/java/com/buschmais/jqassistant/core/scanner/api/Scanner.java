package com.buschmais.jqassistant.core.scanner.api;

import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.configuration.Scan;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;

/**
 * Defines the scanner interface.
 */
public interface Scanner {

    /**
     * Return the scanner configuration.
     */
    Scan getConfiguration();

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
     * @return The {@link Descriptor} as returned by the plugins.
     * @throws IOException
     *             If scanning fails.
     */
    <I, D extends Descriptor> D scan(I item, String path, Scope scope);

    <I, D extends Descriptor> D scan(I item, D descriptor, String path, Scope scope);

    /**
     * Return an instance of the scanner context.
     *
     * @return The scanner context.
     */
    ScannerContext getContext();

    /**
     * Resolve the scope identified by the given fully qualified name.
     *
     * @param name
     *            The fully qualified name.
     * @return The scope.
     */
    Scope resolveScope(String name);
}
