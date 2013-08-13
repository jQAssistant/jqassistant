package com.buschmais.jqassistant.mojo;

import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.impl.EmbeddedGraphStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * A provider for a singleton store.
 */
public class StoreProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreProvider.class);

    private Store store = null;

    /**
     * Return the {@link Store} instance.
     *
     * @param databaseDirectory The database directory to use.
     * @return The {@link Store} instance.
     */
    public Store getStore(final File databaseDirectory) {
        if (store == null) {
            LOGGER.info("Opening store in directory '{}'.", databaseDirectory.getAbsolutePath());
            store = new EmbeddedGraphStore(databaseDirectory.getAbsolutePath());
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    LOGGER.info("Shutting down store in directory '{}'.", databaseDirectory.getAbsolutePath());
                    store.stop();
                }
            });
            store.start();
        }
        return store;
    }
}
