package com.buschmais.jqassistant.core.scanner.impl;

import java.io.File;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the scanner context.
 */
@Slf4j
public class ScannerContextImpl implements ScannerContext {

    private final ClassLoader classLoader;

    private final Store store;

    private final File workingDirectory;

    private final File dataDirectory;

    private Descriptor current;

    private final Map<Class<?>, Deque<?>> contextValuesPerKey = new HashMap<>();

    /**
     * Constructor.
     *
     * @param classLoader
     *     The {@link ClassLoader}.
     * @param store
     *     The store.
     * @param workingDirectory
     *     The working directory
     * @param outputDirectory
     *     The output directory
     */
    public ScannerContextImpl(ClassLoader classLoader, Store store, File workingDirectory, File outputDirectory) {
        this.classLoader = classLoader;
        this.store = store;
        this.workingDirectory = workingDirectory;
        this.dataDirectory = new File(outputDirectory, DATA_DIRECTORY);
    }

    @Override
    public <T> void push(Class<T> key, T value) {
        getValues(key).push(value);
    }

    @Override
    public <T> T peek(Class<T> key) {
        T value = getValues(key).peek();
        if (value == null) {
            throw new IllegalStateException("Cannot find a value for '" + key.getName() + "' in the context");
        }
        return value;
    }

    @Override
    public <T> T peekOrDefault(Class<T> key, T defaultValue) {
        T value = getValues(key).peek();
        return value != null ? value : defaultValue;
    }

    @Override
    public <T> T pop(Class<T> key) {
        return getValues(key).pop();
    }

    @Override
    public <D extends Descriptor> void setCurrentDescriptor(D descriptor) {
        current = descriptor;
    }

    @Override
    public <D extends Descriptor> D getCurrentDescriptor() {
        return (D) current;
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
    private <T> Deque<T> getValues(Class<T> key) {
        return (Deque<T>) contextValuesPerKey.computeIfAbsent(key, k -> new LinkedList<>());
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public Store getStore() {
        return store;
    }

    @Override
    public File getDataDirectory(String path) {
        File directory = new File(dataDirectory, path);
        if (directory.mkdirs()) {
            log.debug("Created data directory '{}'.", directory.getAbsolutePath());
        }
        return directory;
    }

    @Override
    public File getWorkingDirectory() {
        return workingDirectory;
    }
}
