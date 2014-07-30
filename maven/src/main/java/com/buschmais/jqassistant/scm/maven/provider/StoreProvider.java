package com.buschmais.jqassistant.scm.maven.provider;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;

/**
 * Provider holding stores identified by their directory.
 */
@Component(role = StoreProvider.class, instantiationStrategy = "singleton")
public class StoreProvider implements Disposable {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreProvider.class);

    private Map<File, Store> stores = new HashMap<>();

    public StoreProvider() {
        LOGGER.info("Initializing store provider.");
    }

    public Store getStore(File directory) {
        Store store = stores.get(directory);
        if (store == null) {
            LOGGER.info("Opening store in directory '" + directory.getAbsolutePath() + "'.");
            directory.getParentFile().mkdirs();
            store = new EmbeddedGraphStore(directory.getAbsolutePath());
            stores.put(directory, store);
        }
        return store;
    }

    @Override
    public void dispose() {
    }
}
