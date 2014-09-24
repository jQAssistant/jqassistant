package com.buschmais.jqassistant.core.scanner.api;

import com.buschmais.jqassistant.core.store.api.Store;

/**
 * Defines the context for the scanning process.
 *
 * Values of a specific type may be pushed/popped to provide information for
 * plugins.
 */
public interface ScannerContext {

    Store getStore();

    <T> void push(Class<T> key, T value);

    <T> T peek(Class<T> key);

    <T> T pop(Class<T> key);
}
