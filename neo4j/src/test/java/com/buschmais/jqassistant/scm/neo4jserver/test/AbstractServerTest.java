package com.buschmais.jqassistant.scm.neo4jserver.test;

import java.io.IOException;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;

import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.api.RulePluginRepository;
import com.buschmais.jqassistant.core.plugin.api.ScannerPluginRepository;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.scm.neo4jserver.api.Server;
import com.buschmais.jqassistant.scm.neo4jserver.impl.AbstractServer;

/**
 * Abstract base class for server tests.
 */
public class AbstractServerTest extends AbstractJavaPluginIT {

    private Server server;

    @Before
    public void startServer() throws PluginRepositoryException, IOException {
        EmbeddedGraphStore embeddedGraphStore = (EmbeddedGraphStore) store;
        final ScannerPluginRepository scannerPluginRepository = getScannerPluginRepository();
        final RulePluginRepository rulePluginRepository = getRulePluginRepository();

        server = new AbstractServer(embeddedGraphStore) {
            @Override
            protected Iterable<? extends Class<?>> getExtensions() {
                return Collections.emptyList();
            }

            @Override
            protected ScannerPluginRepository getScannerPluginRepository() {
                return scannerPluginRepository;
            }

            @Override
            protected RulePluginRepository getRulePluginRepository() {
                return rulePluginRepository;
            }
        };

        server.start();
    }

    @After
    public void stopServer() {
        server.stop();
    }
}
