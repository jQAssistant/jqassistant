package com.buschmais.jqassistant.core.scanner.api;

import java.io.IOException;
import java.util.Map;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;

/**
 * Defines the interface for plugins for scanning something.
 */
public interface ScannerPlugin<I> {

    /**
     * Initialize the plugin.
     * 
     * @param store
     *            The {@link com.buschmais.jqassistant.core.store.api.Store}
     *            instance to use.
     * @param properties
     *            The plugin properties.
     */
    void initialize(Store store, Map<String, Object> properties);

    Class<? super I> getType();

    boolean accepts(I item, String path, Scope scope) throws IOException;

    Iterable<? extends FileDescriptor> scan(I item, String path, Scope scope, Scanner scanner) throws IOException;

}
