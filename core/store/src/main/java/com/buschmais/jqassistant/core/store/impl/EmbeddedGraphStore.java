package com.buschmais.jqassistant.core.store.impl;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.xo.api.Query.Result.CompositeRowObject;
import com.buschmais.xo.api.XOManager;
import com.buschmais.xo.api.XOManagerFactory;
import com.buschmais.xo.api.bootstrap.XO;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.embedded.api.Neo4jDatastoreSession;
import com.buschmais.xo.neo4j.embedded.api.Neo4jXOProvider;

/**
 * {@link Store} implementation using an embedded Neo4j instance.
 */
public class EmbeddedGraphStore extends AbstractGraphStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedGraphStore.class);

    private static final String PROPERTY_NEO4J_ALLOW_STORE_UPGRADE = "neo4j.allow_store_upgrade";
    private static final String PROPERTY_NEO4J_KEEP_LOGICAL_LOGS = "neo4j.keep_logical_logs";
    private static final String PROPERTY_NEO4J_DBMS_CONNECTOR_BOLT_ENABLED = "neo4j.dbms.connector.bolt.enabled";

    private static final int BATCH_LIMIT = 8192;

    /**
     * The directory of the database.
     */
    private final String databaseDirectory;

    /**
     * Constructor.
     *
     * @param databaseDirectory
     *            The directory of the database.
     */
    public EmbeddedGraphStore(String databaseDirectory) {
        this.databaseDirectory = databaseDirectory;
    }

    @Override
    protected GraphDatabaseService getGraphDatabaseService(XOManager xoManager) {
        return xoManager.getDatastoreSession(Neo4jDatastoreSession.class).getGraphDatabaseService();
    }

    @Override
    protected XOManagerFactory createXOManagerFactory(Collection<Class<?>> types) {
        File database = new File(databaseDirectory);
        Properties properties = new Properties();
        properties.put(PROPERTY_NEO4J_ALLOW_STORE_UPGRADE, Boolean.TRUE.toString());
        properties.put(PROPERTY_NEO4J_KEEP_LOGICAL_LOGS, Boolean.FALSE.toString());
        properties.put(PROPERTY_NEO4J_DBMS_CONNECTOR_BOLT_ENABLED, Boolean.TRUE.toString());
        XOUnit xoUnit = XOUnit.builder().uri(database.toURI()).provider(Neo4jXOProvider.class).types(types).properties(properties)
                .mappingConfiguration(XOUnit.MappingConfiguration.builder().strictValidation(true).build()).build();
        return XO.createXOManagerFactory(xoUnit);
    }

    @Override
    protected void closeXOManagerFactory(XOManagerFactory xoManagerFactory) {
        xoManagerFactory.close();
    }

    @Override
    public void reset() {
        LOGGER.info("Resetting store.");
        long nodes;
        long relations;
        long totalNodes = 0;
        long totalRelations = 0;
        Map<String, Object> params = new HashMap<>();
        params.put("limit", BATCH_LIMIT);
        do {
            beginTransaction();
            CompositeRowObject result = executeQuery(
                    "MATCH (n) OPTIONAL MATCH (n)-[r]-() WITH n, count(r) as rels LIMIT {limit} DETACH DELETE n RETURN count(n) as nodes, sum(rels) as relations",
                    params).getSingleResult();
            nodes = result.get("nodes", Long.class);
            relations = result.get("relations", Long.class);
            commitTransaction();
            totalNodes += nodes;
            totalRelations += relations;
        } while (nodes == BATCH_LIMIT);
        LOGGER.info("Reset finished (removed " + totalNodes + " nodes, " + totalRelations + " relations).");
    }

}
