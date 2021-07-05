package com.buschmais.jqassistant.core.store.api;

import java.net.URI;

import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.core.store.impl.RemoteGraphStore;
import com.buschmais.jqassistant.core.store.spi.StorePluginRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a store instance.
 */
public class StoreFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreFactory.class);

    private StoreFactory() {
    }

    public static Store getStore(StoreConfiguration configuration, StorePluginRepository storePluginRepository) {
        LOGGER.info("Connecting to store at '" + configuration.getUri() + "'"
                + (configuration.getUsername() != null ? " (username=" + configuration.getUsername() + ")" : ""));
        URI uri = configuration.getUri().normalize();
        String scheme = uri.getScheme();
        if (scheme == null) {
            throw new IllegalArgumentException("Cannot determine scheme from URI '" + uri + "'.");
        }
        switch (scheme.toLowerCase()) {
        case "file":
        case "memory":
            return new EmbeddedGraphStore(configuration, storePluginRepository);
        case "bolt":
        case "neo4j":
        case "neo4j+s":
            return new RemoteGraphStore(configuration, storePluginRepository);
        default:
            throw new IllegalArgumentException("Cannot determine store type from URI '" + uri + "'.");
        }
    }
}
