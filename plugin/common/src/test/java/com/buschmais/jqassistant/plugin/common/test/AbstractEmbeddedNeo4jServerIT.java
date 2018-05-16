package com.buschmais.jqassistant.plugin.common.test;

import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.neo4j.backend.bootstrap.EmbeddedNeo4jServer;

import org.junit.After;
import org.junit.Before;

/**
 * Abstract base class for server tests.
 */
public class AbstractEmbeddedNeo4jServerIT extends AbstractPluginIT {

    public static final int SERVER_PORT = 17474;

    private EmbeddedNeo4jServer server;

    @Before
    public void startServer() {
        EmbeddedGraphStore embeddedGraphStore = (EmbeddedGraphStore) store;
        server = embeddedGraphStore.getServer();
        server.start(EmbeddedNeo4jServer.DEFAULT_ADDRESS, SERVER_PORT);
    }

    @After
    public void stopServer() {
        if (server != null) {
            server.stop();
        }
    }
}
