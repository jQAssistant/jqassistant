package com.buschmais.jqassistant.core.store.impl;

import java.io.File;
import java.util.Properties;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.StoreConfiguration;
import com.buschmais.xo.api.XOManager;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.embedded.api.EmbeddedNeo4jDatastoreSession;
import com.buschmais.xo.neo4j.embedded.api.EmbeddedNeo4jXOProvider;

import org.neo4j.graphdb.GraphDatabaseService;

/**
 * {@link Store} implementation using an embedded Neo4j instance.
 */
public class EmbeddedGraphStore extends AbstractGraphStore {

    private static final int AUTOCOMMIT_THRESHOLD = 32678;

    private static final String PROPERTY_NEO4J_ALLOW_STORE_UPGRADE = "neo4j.allow_store_upgrade";
    private static final String PROPERTY_NEO4J_KEEP_LOGICAL_LOGS = "neo4j.keep_logical_logs";
    private static final String PROPERTY_NEO4J_DBMS_ALLOW_FORMAT_MIGRATION = "neo4j.dbms.allow_format_migration";
    private static final String PROPERTY_NEO4J_DBMS_CONNECTOR_BOLT_ENABLED = "neo4j.dbms.connector.bolt.enabled";

    /**
     * Constructor.
     *
     * @param databaseDirectory
     *            The directory of the database.
     */
    @Deprecated
    public EmbeddedGraphStore(String databaseDirectory) {
        super(StoreConfiguration.builder().uri(new File(databaseDirectory).toURI()).build());
    }

    /**
     * Constructor.
     *
     * @param configuration
     *            The configuration.
     */
    public EmbeddedGraphStore(StoreConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected GraphDatabaseService getGraphDatabaseService(XOManager xoManager) {
        return xoManager.getDatastoreSession(EmbeddedNeo4jDatastoreSession.class).getGraphDatabaseService();
    }

    @Override
    protected void configure(XOUnit.XOUnitBuilder builder) {
        builder.provider(EmbeddedNeo4jXOProvider.class);
        Properties properties = new Properties();
        properties.put(PROPERTY_NEO4J_ALLOW_STORE_UPGRADE, Boolean.TRUE.toString());
        properties.put(PROPERTY_NEO4J_KEEP_LOGICAL_LOGS, Boolean.FALSE.toString());
        properties.put(PROPERTY_NEO4J_DBMS_ALLOW_FORMAT_MIGRATION, Boolean.TRUE.toString());
        properties.put(PROPERTY_NEO4J_DBMS_CONNECTOR_BOLT_ENABLED, Boolean.TRUE.toString());
        builder.properties(properties);
    }

    @Override
    protected int getAutocommitThreshold() {
        return AUTOCOMMIT_THRESHOLD;
    }

}
