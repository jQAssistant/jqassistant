package com.buschmais.jqassistant.scm.maven.provider;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.buschmais.jqassistant.core.runtime.api.plugin.PluginRepository;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.StoreFactory;
import com.buschmais.jqassistant.scm.maven.MavenTaskContext;

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

    private final Map<StoreKey, Store> storesByKey = new HashMap<>();

    private final Map<Store, StoreKey> keysByStore = new IdentityHashMap<>();

    /**
     * Create/open store in the given directory.
     *
     * @return The store.
     */
    public Store getStore(MavenTaskContext mavenTaskContext, PluginRepository pluginRepository, Supplier<File> storeDirectorySupplier) {
        com.buschmais.jqassistant.core.store.api.configuration.Store storeConfiguration = mavenTaskContext.getConfiguration()
            .store();
        URI uri = storeConfiguration.uri()
            .orElseGet(() -> {
                File storeDirectory = storeDirectorySupplier.get();
                // ensure directory exists, otherwise URIs don't match (trailing slash is missing if directory does not exist yet)
                storeDirectory.mkdirs();
                return storeDirectory.toURI();
            })
            .normalize();
        StoreKey.StoreKeyBuilder storeKeyBuilder = StoreKey.builder()
            .uri(uri);
        storeConfiguration.remote()
            .username()
            .ifPresent(storeKeyBuilder::username);
        StoreKey key = storeKeyBuilder.build();
        Store store = storesByKey.get(key);
        if (store == null) {
            StoreFactory storeFactory = new StoreFactory(pluginRepository.getStorePluginRepository(), mavenTaskContext.getArtifactProvider());
            store = storeFactory.getStore(storeConfiguration, storeDirectorySupplier);
            store.start();
            storesByKey.put(key, store);
            keysByStore.put(store, key);
        }
        return store;
    }

    /**
     * Close the given store.
     *
     * @param store
     *     The store.
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
     *     The store.
     */
    private void close(Store store) {
        StoreKey key = keysByStore.get(store);
        LOGGER.info("Closing connection to store '{}'.", key.getUri());
        store.stop();
    }
}
