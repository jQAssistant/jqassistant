package com.buschmais.jqassistant.core.store.impl;

import java.util.Iterator;
import java.util.Properties;
import java.util.ServiceLoader;

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

    private static final String PROPERTY_NEO4J_ALLOW_STORE_UPGRADE = "neo4j.allow_store_upgrade";
    private static final String PROPERTY_NEO4J_KEEP_LOGICAL_LOGS = "neo4j.keep_logical_logs";
    private static final String PROPERTY_NEO4J_DBMS_ALLOW_FORMAT_MIGRATION = "neo4j.dbms.allow_format_migration";
    private static final String PROPERTY_NEO4J_DBMS_CONNECTOR_BOLT_ENABLED = "neo4j.dbms.connector.bolt.enabled";

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

    @Deprecated
    public GraphDatabaseService getGraphDatabaseService() {
        LOGGER.warn("Access to the Neo4j GraphDatabaseService should be avoided.");
        return server.getGraphDatabaseService();
    }

    @Override
    protected XOManagerFactory configure(XOUnit.XOUnitBuilder builder) {
        XOManagerFactory xoManagerFactory = getXoManagerFactory(builder);
        this.server = getServer(xoManagerFactory);
        return xoManagerFactory;
    }

    private XOManagerFactory getXoManagerFactory(XOUnit.XOUnitBuilder builder) {
        builder.provider(EmbeddedNeo4jXOProvider.class);
        Properties properties = new Properties();
        properties.put(PROPERTY_NEO4J_ALLOW_STORE_UPGRADE, Boolean.TRUE.toString());
        properties.put(PROPERTY_NEO4J_KEEP_LOGICAL_LOGS, Boolean.FALSE.toString());
        properties.put(PROPERTY_NEO4J_DBMS_ALLOW_FORMAT_MIGRATION, Boolean.TRUE.toString());
        properties.put(PROPERTY_NEO4J_DBMS_CONNECTOR_BOLT_ENABLED, Boolean.TRUE.toString());
        builder.properties(properties);
        return XO.createXOManagerFactory(builder.build());
    }

    private EmbeddedNeo4jServer getServer(XOManagerFactory xoManagerFactory) {
        ServiceLoader<EmbeddedNeo4jServerFactory> serverFactories = ServiceLoader.load(EmbeddedNeo4jServerFactory.class);
        Iterator<EmbeddedNeo4jServerFactory> iterator = serverFactories.iterator();
        EmbeddedNeo4jServer server;
        if (iterator.hasNext()) {
            EmbeddedNeo4jServerFactory serverFactory = iterator.next();
            server = serverFactory.getServer();
        } else {
            throw new IllegalStateException("Cannot find server factory.");
        }
        LOGGER.info("Using embedded Neo4j server " + server.getVersion());
        try (XOManager xoManager = xoManagerFactory.createXOManager()) {
            GraphDatabaseService graphDatabaseService = xoManager.getDatastoreSession(EmbeddedNeo4jDatastoreSession.class).getGraphDatabaseService();
            server.init(graphDatabaseService);
        }
        return server;
    }

    @Override
    protected int getAutocommitThreshold() {
        return AUTOCOMMIT_THRESHOLD;
    }

}
