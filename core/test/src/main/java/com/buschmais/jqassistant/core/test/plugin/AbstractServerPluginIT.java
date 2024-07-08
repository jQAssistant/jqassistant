package com.buschmais.jqassistant.core.test.plugin;

import com.buschmais.jqassistant.core.runtime.api.configuration.ConfigurationBuilder;
import com.buschmais.jqassistant.core.store.api.configuration.Embedded;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.neo4j.embedded.EmbeddedNeo4jServer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Abstract base class for server tests.
 */
public abstract class AbstractServerPluginIT extends AbstractPluginIT {

    private EmbeddedNeo4jServer server;

    @Override
    protected void configure(ConfigurationBuilder configurationBuilder) {
        configurationBuilder.with(Embedded.class, Embedded.CONNECTOR_ENABLED, true);
    }

    @BeforeEach
    void startServer() {
        EmbeddedGraphStore embeddedGraphStore = (EmbeddedGraphStore) store;
        server = embeddedGraphStore.getServer();
        server.start();
    }

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop();
        }
    }
}
