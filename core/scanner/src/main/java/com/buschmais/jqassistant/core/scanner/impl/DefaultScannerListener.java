package com.buschmais.jqassistant.core.scanner.impl;

import com.buschmais.jqassistant.core.scanner.api.ScannerListener;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;

/**
 * The default listener implementation for the scanner.
 * <p>
 * Automatically commits transactions if a defined threshold of scanned items is
 * reached.
 * </p>
 */
public class DefaultScannerListener implements ScannerListener {

    public static final int THRESHOLD = 200;

    private int count = 0;
    private Store store;

    /**
     * Constructor.
     * 
     * @param store
     *            The store.
     */
    public DefaultScannerListener(Store store) {
        this.store = store;
    }

    @Override
    public <I> void before(I item, String relativePath, Scope scope) {
    }

    @Override
    public <I, D extends Descriptor> void after(I item, String relativePath, Scope scope, D descriptor) {
        count++;
        if (count == THRESHOLD) {
            store.commitTransaction();
            store.beginTransaction();
            count = 0;
        }
    }
}
