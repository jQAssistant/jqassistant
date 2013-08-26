package com.buschmais.jqassistant.mojo;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * A provider for a singleton store.
 */
public class StoreProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreProvider.class);

    private Map<File, Store> stores = new HashMap<>();

    public StoreProvider() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                LOGGER.info("Shutting down stores...");
                for (Map.Entry<File, Store> entry : stores.entrySet()) {
                    LOGGER.info("  '{}'", entry.getKey().getAbsolutePath());
                    entry.getValue().stop();
                }
                LOGGER.info("Shutdown finished.");
            }
        });
    }

    /**
     * Return the {@link Store} instance.
     *
     * @param databaseDirectory The database directory to use.
     * @return The {@link Store} instance.
     */
    public Store getStore(final File databaseDirectory) {
        Store store = stores.get(databaseDirectory);
        if (store == null) {
            LOGGER.info("Opening store in directory '{}'.", databaseDirectory.getAbsolutePath());
            databaseDirectory.getParentFile().mkdirs();
            store = new EmbeddedGraphStore(databaseDirectory.getAbsolutePath());
            store.start();
            stores.put(databaseDirectory, store);
        }
        return store;
    }
}
