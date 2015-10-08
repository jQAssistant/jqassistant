package com.buschmais.jqassistant.plugin.common.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;

/**
 * Defines a strategy for resolving a file.
 */
public interface FileResolverStrategy {

    /**
     * Resolve an existing descriptor from the given information.
     *
     * This is usually done by evaluating the given path, e.g. if a path
     * "com/buschmais/Test.class" is given a class file resolver might return an
     * existing class descriptor with the fully qualified name
     * "com.buschmais.Test" which has been created before as a referenced class.
     * 
     * @param path
     *            The path.
     * @param context
     *            The scanner context.
     * @return The resolved descriptor or <code>null</code>.
     */
    Descriptor require(String path, ScannerContext context);

    Descriptor match(String path, ScannerContext context);
}
