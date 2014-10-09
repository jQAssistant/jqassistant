package com.buschmais.jqassistant.scm.neo4jserver.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.Test;
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
import com.buschmais.jqassistant.scm.neo4jserver.impl.rest.MetricsService;

/**
 * "http://localhost:7474/jqa/rest/scan?url=http://search.maven.org/remotecontent?filepath=org/eclipse/birt/runtime/org.eclipse.birt.runtime/4.2.0/org.eclipse.birt.runtime-4.2.0.jar"
 */
public class ServerMT extends AbstractDatabaseIT {

    @Test
    public void server() throws IOException, PluginRepositoryException {
        EmbeddedGraphStore store = createStore();
        PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl();
        final ModelPluginRepository modelPluginRepository = new ModelPluginRepositoryImpl(pluginConfigurationReader);
        final ScannerPluginRepository scannerPluginRepository = new ScannerPluginRepositoryImpl(pluginConfigurationReader, new HashMap<String, Object>());
        final RulePluginRepository rulePluginRepository = new RulePluginRepositoryImpl(pluginConfigurationReader);
        store.start(modelPluginRepository.getDescriptorTypes());
        GraphDatabaseAPI databaseAPI = store.getDatabaseService();
        Server server = new AbstractServer(databaseAPI, store) {
            @Override
            protected Iterable<? extends Class<?>> getExtensions() {
                return Arrays.asList(MetricsService.class);
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
        System.out.println("Hit Enter to continue.");
        System.in.read();
        server.stop();
        store.stop();
    }

    @Override
    protected String getStoreDir() {
        return "D:\\dev\\jqassistant_core\\core\\target\\jqassistant\\store";
    }
}
