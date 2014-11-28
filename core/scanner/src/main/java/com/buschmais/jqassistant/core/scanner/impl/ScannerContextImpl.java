package com.buschmais.jqassistant.core.scanner.impl;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;

/**
 * Implementation of the scanner context.
 */
public class ScannerContextImpl implements ScannerContext {

    private final Store store;

    private final Map<Class<?>, Deque<?>> contextValuesPerKey = new HashMap<>();

    private final Map<String, Scope> scopes;

    /**
     * Constructor.
     * 
     * @param store
     *            The store.
     */
    public ScannerContextImpl(Store store, Map<String, Scope> scopes) {
        this.store = store;
        this.scopes = scopes;
    }

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

    @Override
    public Scope resolveScope(String name) {
        return scopes.get(name);
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
        Deque<T> values = (Deque<T>) contextValuesPerKey.get(key);
        if (values == null) {
            values = new LinkedList<>();
            contextValuesPerKey.put(key, values);
        }
        return values;
    }

    @Override
    public Store getStore() {
        return store;
    }
}
