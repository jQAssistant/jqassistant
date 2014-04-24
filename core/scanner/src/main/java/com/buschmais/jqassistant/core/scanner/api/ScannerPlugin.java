package com.buschmais.jqassistant.core.scanner.api;

import java.util.Properties;

import com.buschmais.jqassistant.core.store.api.Store;

/**
 * Defines the interface for plugins for scanning something.
 */
public interface ScannerPlugin {

    /**
     * Initialize the plugin.
     * 
     * @param store
     *            The {@link com.buschmais.jqassistant.core.store.api.Store}
     *            instance to use.
     * @param properties
     *            The plugin properties.
     */
    void initialize(Store store, Properties properties);

}
