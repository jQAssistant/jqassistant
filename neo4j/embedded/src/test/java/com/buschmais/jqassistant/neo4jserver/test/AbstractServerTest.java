package com.buschmais.jqassistant.neo4jserver.test;

import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.neo4jserver.bootstrap.impl.EmbeddedNeoServer;
import com.buschmais.jqassistant.neo4jserver.bootstrap.spi.Server;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

import org.junit.After;
import org.junit.Before;

/**
 * Abstract base class for server tests.
 */
public class AbstractServerTest extends AbstractPluginIT {

    public static final int SERVER_PORT = 17474;

    private Server server;

    @Before
    public void startServer() {
        EmbeddedGraphStore embeddedGraphStore = (EmbeddedGraphStore) store;
        server = new EmbeddedNeoServer();
        server.init(embeddedGraphStore.getGraphDatabaseService());
        server.start(Server.DEFAULT_ADDRESS, SERVER_PORT);
    }

    @After
    public void stopServer() {
        if (server != null) {
            server.stop();
        }
    }
}
