package com.buschmais.jqassistant.mojo;

import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.impl.EmbeddedGraphStore;

import java.io.File;

/**
 */
public class StoreProvider {

    private Store store = null;

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
