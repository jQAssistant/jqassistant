package com.buschmais.jqassistant.scm.maven.provider;

import java.io.File;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;

/**
 * Manages the life cycle of {@link Store} instance.
 */
@Singleton
public class StoreFactory implements Disposable {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreFactory.class);

    private Map<Store, File> stores = new IdentityHashMap<>();

    /**
     * Create/open store in the given directory.
     * 
     * @param directory
     *            The directory.
     * @param types
     *            The types to register.
     * @return The store.
     */
    public Store createStore(File directory, List<Class<?>> types) {
        LOGGER.info("Opening store in directory '" + directory.getAbsolutePath() + "'.");
        directory.getParentFile().mkdirs();
        Store store = new EmbeddedGraphStore(directory.getAbsolutePath());
        store.start(types);
        stores.put(store, directory);
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
        File directory = stores.get(store);
        LOGGER.info("Closing store in directory '" + directory.getAbsolutePath() + "'.");
        store.stop();
    }

}
