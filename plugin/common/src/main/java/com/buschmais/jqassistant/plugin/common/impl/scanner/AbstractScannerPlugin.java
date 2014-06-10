package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.store.api.Store;

/**
 * Abstract base implementation of a {@link ScannerPlugin}.
 */
public abstract class AbstractScannerPlugin<I> implements ScannerPlugin<I> {

    private Store store;

    private Map<String, Object> properties;

    @Override
    public void initialize(Store store, Map<String, Object> properties) {
        this.store = store;
        this.properties = properties;
        initialize();
    }

    /**
     * Initialize the plugin.
     */
    protected abstract void initialize();

    protected Store getStore() {
        return store;
    }

    protected Map<String, Object> getProperties() {
        return properties;
    }
}
