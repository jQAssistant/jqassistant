package com.buschmais.jqassistant.core.scanner.api;

import java.io.File;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;

/**
 * Defines the context for the scanning process.
 *
 * Values of a specific type may be pushed/popped to provide information for
 * plugins.
 */
public interface ScannerContext {

    String DATA_DIRECTORY = "data";

    /**
     * Return the plugin {@link ClassLoader}.
     *
     * @return The plugin {@link ClassLoader}
     */
    ClassLoader getClassLoader();

    /**
     * Return the store.
     *
     * @return The store.
     */
    Store getStore();

    /**
     * Push a value to the context.
     *
     * @param key
     *            The type of the value.
     * @param value
     *            The value.
     * @param <T>
     *            The type of the value.
     */
    <T> void push(Class<T> key, T value);

    /**
     * Peek for a value in the context.
     *
     * @param key
     *            The type of the value.
     * @param <T>
     *            The type of the value.
     * @return The value.
     * @throws IllegalStateException
     *             If the context does not provide a value for the given key.
     */
    <T> T peek(Class<T> key);

    /**
     * Peek for a value in the context. If no value is available return the
     * provided default value.
     *
     * @param key
     *            The type of the value.
     * @param defaultValue
     *            The default value to return.
     * @param <T>
     *            The type of the value.
     * @return The value.
     */
    <T> T peekOrDefault(Class<T> key, T defaultValue);

    /**
     * Pop a value from the context.
     *
     * @param key
     *            The type of the value.
     * @param <T>
     *            The type of the value.
     * @return The value.
     */
    <T> T pop(Class<T> key);

    /**
     * Set the descriptor which is currently enhanced by the scanner plugins in
     * the pipeline.
     *
     * @param descriptor
     *            The descriptor.
     * @param <D>
     *            The descriptor type.
     */
    <D extends Descriptor> void setCurrentDescriptor(D descriptor);

    /**
     * Return the descriptor which is currently enhanced by the scanner plugins
     * in the pipeline.
     *
     * @param <D>
     *            The descriptor type.
     */
    <D extends Descriptor> D getCurrentDescriptor();

    /**
    * Return the directory for storing data using a given relative
     */
    File getDataDirectory(String path);
}
