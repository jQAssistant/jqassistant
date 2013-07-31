package com.buschmais.jqassistant.mojo;

import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.impl.EmbeddedGraphStore;

import java.io.File;

/**
 * A provider for a singleton store.
 */
public class StoreProvider {

    private Store store = null;

    /**
     * Return the {@link Store} instance.
     *
     * @param databaseDirectory The database directory to use.
     * @return The {@link Store} instance.
     */
    public Store getStore(File databaseDirectory) {
        if (store == null) {
            store = new EmbeddedGraphStore(databaseDirectory.getAbsolutePath());
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    store.stop();
                }
            });
            store.start();
        }
        return store;
    }

}
