package com.buschmais.jqassistant.core.store.impl;

import java.util.Iterator;
import java.util.ServiceLoader;

import com.buschmais.jqassistant.core.shared.annotation.ToBeRemovedInVersion;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.StoreConfiguration;
import com.buschmais.jqassistant.neo4j.backend.bootstrap.EmbeddedNeo4jServer;
import com.buschmais.jqassistant.neo4j.backend.bootstrap.EmbeddedNeo4jServerFactory;
import com.buschmais.xo.api.XOManager;
import com.buschmais.xo.api.XOManagerFactory;
import com.buschmais.xo.api.bootstrap.XO;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.embedded.api.EmbeddedNeo4jDatastoreSession;
import com.buschmais.xo.neo4j.embedded.api.EmbeddedNeo4jXOProvider;

import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * {@link Store} implementation using an embedded Neo4j instance.
 */
public class EmbeddedGraphStore extends AbstractGraphStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedGraphStore.class);

    private static final int AUTOCOMMIT_THRESHOLD = 32678;

    private EmbeddedNeo4jServer server;

    /**
     * Constructor.
     *
     * @param configuration
     *            The configuration.
     */
    public EmbeddedGraphStore(StoreConfiguration configuration) {
        super(configuration);
    }

    public EmbeddedNeo4jServer getServer() {
        return server;
    }

    @ToBeRemovedInVersion(major = 1, minor = 5)
    @Deprecated
    public GraphDatabaseService getGraphDatabaseService() {
        LOGGER.warn("Access to the Neo4j GraphDatabaseService is deprecated.");
        return server.getGraphDatabaseService();
    }

    @Override
    protected XOManagerFactory configure(XOUnit.XOUnitBuilder builder) {
        EmbeddedNeo4jServerFactory serverFactory = getEmbeddedNeo4jServerFactory();
        builder.provider(EmbeddedNeo4jXOProvider.class);
        serverFactory.configure(builder);
        this.server = serverFactory.getServer();
        LOGGER.info("Using embedded Neo4j server " + server.getVersion());
        XOManagerFactory xoManagerFactory = XO.createXOManagerFactory(builder.build());
        try (XOManager xoManager = xoManagerFactory.createXOManager()) {
            GraphDatabaseService graphDatabaseService = xoManager.getDatastoreSession(EmbeddedNeo4jDatastoreSession.class).getGraphDatabaseService();
            server.init(graphDatabaseService);
        }
        return xoManagerFactory;
    }

    private EmbeddedNeo4jServerFactory getEmbeddedNeo4jServerFactory() {
        ServiceLoader<EmbeddedNeo4jServerFactory> serverFactories = ServiceLoader.load(EmbeddedNeo4jServerFactory.class);
        Iterator<EmbeddedNeo4jServerFactory> iterator = serverFactories.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            throw new IllegalStateException("Cannot find server factory.");
        }
    }

    @Override
    protected int getAutocommitThreshold() {
        return AUTOCOMMIT_THRESHOLD;
    }

}
