package com.buschmais.jqassistant.core.store.api;

import java.io.File;
import java.net.URI;
import java.util.function.Supplier;

import com.buschmais.jqassistant.core.shared.artifact.ArtifactProvider;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.core.store.impl.RemoteGraphStore;
import com.buschmais.jqassistant.core.store.spi.StorePluginRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a store instance.
 */
@RequiredArgsConstructor
public class StoreFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreFactory.class);

    private final StorePluginRepository storePluginRepository;

    private final ArtifactProvider artifactProvider;

    public Store getStore(com.buschmais.jqassistant.core.store.api.configuration.Store configuration, Supplier<File> storeDirectorySupplier) {
        URI uri = configuration.uri()
            .orElse(storeDirectorySupplier.get()
                .toURI())
            .normalize();
        LOGGER.info("Connecting to store at {}'", uri);
        String scheme = uri.getScheme();
        if (scheme == null) {
            throw new IllegalArgumentException("Cannot determine scheme from URI '" + uri + "'.");
        }
        switch (scheme.toLowerCase()) {
        case "file":
        case "memory":
            return new EmbeddedGraphStore(uri, configuration, storePluginRepository, artifactProvider);
        case "bolt":
        case "neo4j":
        case "neo4j+s":
            configuration.remote()
                .username()
                .ifPresent(username -> LOGGER.info("Authenticating with user '{}'", username));
            return new RemoteGraphStore(uri, configuration, storePluginRepository);
        default:
            throw new IllegalArgumentException("Cannot determine store type from URI '" + uri + "'.");
        }
    }
}
