package com.buschmais.jqassistant.core.scanner.impl;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;

/**
 * Implementation of the scanner context.
 */
public class ScannerContextImpl implements ScannerContext {

    private Map<Class<?>, Deque<?>> valuesPerKey = new HashMap<>();

    @Override
    public <T> void push(Class<T> key, T value) {
        getValues(key).push(value);
    }

    @Override
    public <T> T peek(Class<T> key) {
        return getValues(key).peek();
    }

    @Override
    public <T> T pop(Class<T> key) {
        return getValues(key).pop();
    }

    /**
     * Determine the stack for the given key.
     * 
     * @param key
     *            The key.
     * @param <T>
     *            The key key.
     * @return The stack.
     */
    <T> Deque<T> getValues(Class<T> key) {
        Deque<T> values = (Deque<T>) valuesPerKey.get(key);
        if (values == null) {
            values = new LinkedList<>();
            valuesPerKey.put(key, values);
        }
        return values;
    }
}
