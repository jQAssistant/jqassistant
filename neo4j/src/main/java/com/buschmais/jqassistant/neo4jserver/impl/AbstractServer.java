package com.buschmais.jqassistant.neo4jserver.impl;

import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.server.WrappingNeoServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.neo4jserver.api.Server;

/**
 * Abstract base class for the customized Neo4j server.
 *
 */
public abstract class AbstractServer extends WrappingNeoServer implements Server {

    /**
     * The logger class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractServer.class);

    protected final Store store;

    public AbstractServer(EmbeddedGraphStore graphStore) {
        super((GraphDatabaseAPI) graphStore.getGraphDatabaseService());
        this.store = graphStore;
    }

}
