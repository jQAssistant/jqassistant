package com.buschmais.jqassistant.scm.neo4jserver.ui;

import java.util.Collections;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.neo4j.kernel.GraphDatabaseAPI;

import com.buschmais.jqassistant.core.plugin.api.ModelPluginRepository;
import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.api.RulePluginRepository;
import com.buschmais.jqassistant.core.plugin.api.ScannerPluginRepository;
import com.buschmais.jqassistant.core.plugin.impl.ModelPluginRepositoryImpl;
import com.buschmais.jqassistant.core.plugin.impl.PluginConfigurationReaderImpl;
import com.buschmais.jqassistant.core.plugin.impl.RulePluginRepositoryImpl;
import com.buschmais.jqassistant.core.plugin.impl.ScannerPluginRepositoryImpl;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.scm.neo4jserver.api.Server;
import com.buschmais.jqassistant.scm.neo4jserver.impl.AbstractServer;
import com.buschmais.jqassistant.scm.neo4jserver.test.AbstractDatabaseIT;

/**
 * Abstract base class for HTML tests.
 */
public class AbstractHtmlTest extends AbstractDatabaseIT {

    private EmbeddedGraphStore store;
    private Server server;

    @Before
    public void startServer() throws PluginRepositoryException {

        store = createStore();

        PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl();
        final ModelPluginRepository modelPluginRepository = new ModelPluginRepositoryImpl(pluginConfigurationReader);
        final ScannerPluginRepository scannerPluginRepository = new ScannerPluginRepositoryImpl(pluginConfigurationReader, new HashMap<String, Object>());
        final RulePluginRepository rulePluginRepository = new RulePluginRepositoryImpl(pluginConfigurationReader);

        store.start(modelPluginRepository.getDescriptorTypes());

        GraphDatabaseAPI databaseAPI = store.getDatabaseService();
        server = new AbstractServer(databaseAPI, store) {
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
        store.stop();
    }

    @Override
    protected String getStoreDir() {

        // TODO how to get the store directory?
        return "D:\\dev\\jqassistant_core\\core\\target\\jqassistant\\store";
    }
}
