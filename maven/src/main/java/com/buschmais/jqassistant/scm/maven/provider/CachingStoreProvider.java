package com.buschmais.jqassistant.scm.maven.provider;

import java.net.URI;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.plugin.api.PluginRepository;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.StoreConfiguration;
import com.buschmais.jqassistant.core.store.api.StoreFactory;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the life cycle of {@link Store} instance.
 */
@Component(role = CachingStoreProvider.class, instantiationStrategy = "singleton")
public class CachingStoreProvider implements Disposable {

    @Getter
    @Builder
    @EqualsAndHashCode
    @ToString
    private static class StoreKey {

        private final URI uri;

        private final String username;

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(CachingStoreProvider.class);

    private Map<StoreKey, Store> storesByKey = new HashMap<>();

    private Map<Store, StoreKey> keysByStore = new IdentityHashMap<>();

    /**
     * Create/open store in the given directory.
     *
     * @param storeConfiguration
     *            The store configuration.
     * @param pluginRepository
     *            The pluginRepository.
     * @return The store.
     */
    public Store getStore(StoreConfiguration storeConfiguration, PluginRepository pluginRepository) {
        StoreKey key = StoreKey.builder().uri(storeConfiguration.getUri().normalize()).username(storeConfiguration.getUsername()).build();
        Store store = storesByKey.get(key);
        if (store == null) {
            store = StoreFactory.getStore(storeConfiguration);
            List<Class<?>> descriptorTypes = pluginRepository.getStorePluginRepository().getDescriptorTypes();
            List<Class<?>> procedureTypes = pluginRepository.getStorePluginRepository().getProcedureTypes();
            List<Class<?>> functionTypes = pluginRepository.getStorePluginRepository().getFunctionTypes();
            store.start(descriptorTypes, procedureTypes, functionTypes);
            storesByKey.put(key, store);
            keysByStore.put(store, key);
        }
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
        StoreKey key = keysByStore.remove(store);
        storesByKey.remove(key);
    }

    @Override
    public void dispose() {
        for (Store store : storesByKey.values()) {
            close(store);
        }
        storesByKey.clear();
        keysByStore.clear();
    }

    /**
     * Close the given store.
     *
     * @param store
     *            The store.
     */
    private void close(Store store) {
        StoreKey key = keysByStore.get(store);
        LOGGER.info("Closing store in directory '" + key.getUri() + "'.");
        store.stop();
    }
}
