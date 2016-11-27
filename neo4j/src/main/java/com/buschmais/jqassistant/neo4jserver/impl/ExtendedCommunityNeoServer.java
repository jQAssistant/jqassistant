package com.buschmais.jqassistant.neo4jserver.impl;

import org.apache.commons.configuration.Configuration;
import org.neo4j.server.configuration.ServerSettings;
import org.neo4j.server.security.ssl.KeyStoreInformation;

import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;

/**
 * The customized Neo4j server.
 */
public class ExtendedCommunityNeoServer extends AbstractServer {

    public static final String DEFAULT_ADDRESS = "localhost";

    public static final int DEFAULT_PORT = 7474;

    /**
     * Constructor.
     * 
     * @param graphStore
     *            The store instance to use.
     * @param port
     *            The port number of the server.
     */
    public ExtendedCommunityNeoServer(EmbeddedGraphStore graphStore, String address, int port) {
        super(graphStore);
        init(address, port);
    }

    /**
     * Initialize the server.
     * 
     * @param address
     *            The address to use for binding the server.
     * @param port
     *            The HTTP port to use.
     */
    private void init(String address, int port) {
        Configuration configuration = getConfigurator().configuration();
        configuration.setProperty(ServerSettings.webserver_address.name(), address);
        configuration.setProperty(ServerSettings.webserver_port.name(), Integer.toString(port));
        configuration.setProperty(ServerSettings.auth_enabled.name(), Boolean.FALSE.toString());
    }

    @Override
    protected KeyStoreInformation createKeyStore() {
        // fixes issue #255, the default implementation creates a directory
        // "neo4j-home" containing SSL certificates.
        return null;
    }

}
