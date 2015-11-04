package com.buschmais.jqassistant.plugin.common.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;

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
    <D extends FileDescriptor> D require(String path, Class<D> type, ScannerContext context);

    /**
     * Match an existing descriptor in the store and return it with as the given
     * type if it exists.
     * 
     * Example: A Java class might exist with a fully qualified name in the
     * database. The implementation of this method should check if the given
     * path can be transformed into a class name (i.e. replacing '/' with '.')
     * that already exists as descriptor (i.e. node) and return it.
     * 
     * @param path
     *            The path.
     * @param type
     *            The expected type.
     * @param context
     *            The scanner context.
     * @param <D>
     *            The expected type.
     * @return The matching descriptor or <code>null</code>.
     */
    <D extends FileDescriptor> D match(String path, Class<D> type, ScannerContext context);
}
