package com.buschmais.jqassistant.scm.neo4jserver.impl;

import org.neo4j.server.database.InjectableProvider;

import com.buschmais.jqassistant.core.store.api.Store;
import com.sun.jersey.api.core.HttpContext;

/**
 * A provider for the underlying store instance.
 */
public class StoreProvider extends InjectableProvider<Store> {

    private final Store store;

    /**
     * Constructor.
     * @param store The store instance.
     */
    public StoreProvider(Store store) {
        super(Store.class);
        this.store = store;
    }

    @Override
    public Store getValue(HttpContext c) {
        return store;
    }
}
