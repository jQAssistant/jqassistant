package com.buschmais.jqassistant.core.scanner.api;

import com.buschmais.jqassistant.core.store.api.Store;

/**
 * Defines the context for the scanning process.
 *
 * Values of a specific type may be pushed/popped to provide information for
 * plugins.
 */
public interface ScannerContext {

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

}
