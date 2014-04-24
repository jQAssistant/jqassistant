package com.buschmais.jqassistant.scm.maven;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;

/**
 * Repository holding stores identified by their directory.
 */
@Component(role = StoreRepository.class, instantiationStrategy = "singleton")
public class StoreRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreRepository.class);

    private Map<File, Store> stores = new HashMap<>();

    public StoreRepository() {
        LOGGER.info("Initializing store repository.");
    }

    public Store getStore(File directory, boolean reset) {
        Store store = stores.get(directory);
        if (store == null) {
            LOGGER.info("Opening store in directory '" + directory.getAbsolutePath() + "'");
            directory.getParentFile().mkdirs();
            store = new EmbeddedGraphStore(directory.getAbsolutePath());
            if (reset) {
                // reset the store if the current project is the base project
                // (i.e. where the rules are located).
                store.start(Collections.<Class<?>> emptyList());
                store.reset();
                store.stop();
            }
            stores.put(directory, store);
        }
        return store;
    }
}
