package com.buschmais.jqassistant.scm.neo4jserver.impl;

import static java.util.Collections.emptyList;

import org.apache.commons.configuration.Configuration;
import org.neo4j.server.configuration.ServerSettings;

import com.buschmais.jqassistant.core.plugin.api.RulePluginRepository;
import com.buschmais.jqassistant.core.plugin.api.ScannerPluginRepository;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;

/**
 * The customized Neo4j server.
 * <p>
 * The class adds the {@link JQAServerModule}
 * </p>
 */
public class ExtendedCommunityNeoServer extends AbstractServer {

    public static final String DEFAULT_ADDRESS = "localhost";

    public static final int DEFAULT_PORT = 7474;

    private ScannerPluginRepository scannerPluginRepository;

    private RulePluginRepository rulePluginRepository;

    /**
     * Constructor.
     * 
     * @param graphStore
     *            The store instance to use.
     * @param scannerPluginRepository
     *            The scanner plugin repository.
     * @param rulePluginRepository
     *            The rule plugin repository.
     * @param port
     *            The port number of the server.
     */
    public ExtendedCommunityNeoServer(EmbeddedGraphStore graphStore, ScannerPluginRepository scannerPluginRepository,
                                      RulePluginRepository rulePluginRepository, String address, int port) {
        super(graphStore);
        init(scannerPluginRepository, rulePluginRepository, address, port);
    }

    /**
     * Initialize the server.
     * 
     * @param scannerPluginRepository
     *            The scanner plugin repository.
     * @param rulePluginRepository
     *            The rule plugin repository.
     * @param address
     *            The address to use for binding the server.
     * @param port
     *            The HTTP port to use.
     */
    private void init(ScannerPluginRepository scannerPluginRepository, RulePluginRepository rulePluginRepository, String address, int port) {
        this.scannerPluginRepository = scannerPluginRepository;
        this.rulePluginRepository = rulePluginRepository;
        Configuration configuration = getConfigurator().configuration();
        configuration.setProperty(ServerSettings.webserver_address.name(), address);
        configuration.setProperty(ServerSettings.webserver_port.name(), Integer.toString(port));
        configuration.setProperty(ServerSettings.auth_enabled.name(), Boolean.FALSE.toString());
    }

    @Override
    protected Iterable<? extends Class<?>> getExtensions() {
        return emptyList();
    }

    @Override
    protected ScannerPluginRepository getScannerPluginRepository() {
        return scannerPluginRepository;
    }

    @Override
    protected RulePluginRepository getRulePluginRepository() {
        return rulePluginRepository;
    }

}
