package com.buschmais.jqassistant.plugin.common.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;

/**
 * Defines a strategy for resolving a file.
 */
public interface FileResolver {

    /**
     * Resolve an existing file descriptor for the given paths.
     * <p>
     * This is usually done by evaluating the given path, e.g. if a path
     * "com/buschmais/Test.class" is given a class file resolver might return an
     * existing class descriptor with the fully qualified name
     * "com.buschmais.Test" which has been created before as a referenced class.
     *
     * @param requiredPath
     *            The path of the file to require, e.g. /com/acme/Example.class
     * @param containedPath
     *            The internal path (e.g. within the the same artifact if
     *            applicable), e.g. /WEB-INF/classes/com/acme/Example.class
     * @param type
     *            The file descriptor type.
     * @param context
     *            The scanner context.
     * @return The resolved file descriptor.
     */
    <D extends FileDescriptor> D require(String requiredPath, String containedPath, Class<D> type, ScannerContext context);

    /**
     * Resolve an existing descriptor from the given information.
     * <p>
     * This is a convenience method delegating to
     * {@link #require(String, Class, ScannerContext)} using the value of path
     * also as mappedPath.
     *
     * @param requiredPath
     *            The path of the file to require, e.g. /com/acme/Example.class
     * @param type
     *            The file descriptor type.
     * @param context
     *            The scanner context.
     * @param <D>
     *            The expected file descriptor type.
     * @return The resolved file descriptor.
     */
    <D extends FileDescriptor> D require(String requiredPath, Class<D> type, ScannerContext context);

    /**
     * Match an existing descriptor in the store and return it with as the given
     * type if it exists.
     * <p>
     * Example: A Java class might exist with a fully qualified name in the
     * database. The implementation of this method should check if the given
     * path can be transformed into a class name (i.e. replacing '/' with '.')
     * that already exists as descriptor (i.e. node) and return it.
     *
     * @param containedPath
     *            The path.
     * @param type
     *            The expected type.
     * @param context
     *            The scanner context.
     * @param <D>
     *            The expected type.
     * @return The matching descriptor.
     */
    <D extends FileDescriptor> D match(String containedPath, Class<D> type, ScannerContext context);
}
