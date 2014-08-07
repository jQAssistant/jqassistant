package com.buschmais.jqassistant.scm.maven.provider;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;

/**
 * Manages the life cycle of {@link Store} instance.
 */
@Component(role = StoreFactory.class, instantiationStrategy = "singleton")
public class StoreFactory implements Disposable {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreFactory.class);

    private Map<File, Store> stores = new HashMap<>();

    public Store createStore(File directory, List<Class<?>> types) {
        LOGGER.info("Opening store in directory '" + directory.getAbsolutePath() + "'.");
        directory.getParentFile().mkdirs();
        Store store = new EmbeddedGraphStore(directory.getAbsolutePath());
        store.start(types);
        stores.put(directory, store);
        return store;
    }

    @Override
    public void dispose() {
        for (Store store : stores.values()) {
            store.stop();
        }
    }
}
