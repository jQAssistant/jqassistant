package com.buschmais.jqassistant.neo4jserver.test;

import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.api.RulePluginRepository;
import com.buschmais.jqassistant.core.plugin.api.ScannerPluginRepository;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.neo4jserver.api.Server;
import com.buschmais.jqassistant.neo4jserver.impl.ExtendedCommunityNeoServer;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;


/**
 * Abstract base class for server tests.
 */
public class AbstractServerTest extends AbstractJavaPluginIT {

    public static final int SERVER_PORT = 17474;

    private Server server;

    @Before
    public void startServer() throws PluginRepositoryException, IOException {
        EmbeddedGraphStore embeddedGraphStore = (EmbeddedGraphStore) store;
        final ScannerPluginRepository scannerPluginRepository = getScannerPluginRepository();
        final RulePluginRepository rulePluginRepository = getRulePluginRepository();
        server = new ExtendedCommunityNeoServer(embeddedGraphStore, scannerPluginRepository, rulePluginRepository, ExtendedCommunityNeoServer.DEFAULT_ADDRESS, SERVER_PORT);
        server.start();
    }

    @After
    public void stopServer() {
        server.stop();
    }
}
