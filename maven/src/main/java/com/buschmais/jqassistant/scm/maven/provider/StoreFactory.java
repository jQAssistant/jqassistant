package com.buschmais.jqassistant.scm.maven.provider;

import java.net.URI;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.StoreConfiguration;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the life cycle of {@link Store} instance.
 */
@Singleton
public class StoreFactory implements Disposable {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreFactory.class);

    private Map<Store, URI> stores = new IdentityHashMap<>();

    /**
     * Create/open store in the given directory.
     * 
     * @param storeConfiguration
     *            The store configuration.
     * @param types
     *            The types to register.
     * @return The store.
     */
    public Store createStore(StoreConfiguration storeConfiguration, List<Class<?>> types) {
        Store store = com.buschmais.jqassistant.core.store.api.StoreFactory.getStore(storeConfiguration);
        store.start(types);
        stores.put(store, storeConfiguration.getUri());
        return store;
    }

    /**
     * Close the given store.
     * 
     * @param store
     *            The store.
     */
    public void closeStore(Store store) {
        close(store);
        stores.remove(store);
    }

    @Override
    public void dispose() {
        for (Store store : stores.keySet()) {
            close(store);
        }
    }

    /**
     * Close the given store.
     * 
     * @param store
     *            The store.
     */
    private void close(Store store) {
        URI uri = stores.get(store);
        LOGGER.info("Closing store in directory '" + uri + "'.");
        store.stop();
    }

}
