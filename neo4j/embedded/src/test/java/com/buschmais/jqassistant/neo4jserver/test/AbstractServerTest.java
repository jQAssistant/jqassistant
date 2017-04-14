package com.buschmais.jqassistant.neo4jserver.test;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;

import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.neo4jserver.bootstrap.api.Server;
import com.buschmais.jqassistant.neo4jserver.bootstrap.impl.EmbeddedNeoServer;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

/**
 * Abstract base class for server tests.
 */
public class AbstractServerTest extends AbstractPluginIT {

    public static final int SERVER_PORT = 17474;

    private Server server;

    @Before
    public void startServer() throws IOException {
        EmbeddedGraphStore embeddedGraphStore = (EmbeddedGraphStore) store;
        server = new EmbeddedNeoServer(embeddedGraphStore, Server.DEFAULT_ADDRESS, SERVER_PORT);
        server.start();
    }

    @After
    public void stopServer() {
        if (server != null) {
            server.stop();
        }
    }
}
