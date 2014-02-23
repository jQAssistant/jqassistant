package com.buschmais.jqassistant.plugin.common.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;

import java.util.Properties;

/**
 * Abstract base implementation of a {@link FileScannerPlugin}.
 */
public abstract class AbstractFileScannerPlugin<T extends Descriptor> implements FileScannerPlugin<T> {

    private Store store;

    private Properties properties;

    @Override
    public void initialize(Store store, Properties properties) {
        this.store = store;
        this.properties = properties;
        initialize();
    }

    /**
     * Initialize the concrete plugin.
     */
    protected abstract void initialize();

    protected Store getStore() {
        return store;
    }

    protected Properties getProperties() {
        return properties;
    }
}
