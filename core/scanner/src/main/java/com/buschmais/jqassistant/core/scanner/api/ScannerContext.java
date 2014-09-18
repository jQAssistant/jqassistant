package com.buschmais.jqassistant.core.scanner.api;

/**
 * Defines the context for the scanning process.
 *
 * Values of a specific type may be pushed/popped to provide information for
 * plugins.
 */
public interface ScannerContext {

    <T> void push(Class<T> key, T value);

    <T> T peek(Class<T> key);

    <T> T pop(Class<T> key);
}
