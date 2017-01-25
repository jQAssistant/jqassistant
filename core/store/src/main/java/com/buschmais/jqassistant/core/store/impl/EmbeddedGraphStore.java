package com.buschmais.jqassistant.core.store.impl;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.xo.api.XOManager;
import com.buschmais.xo.api.XOManagerFactory;
import com.buschmais.xo.api.bootstrap.XO;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.api.bootstrap.XOUnitBuilder;
import com.buschmais.xo.neo4j.api.Neo4jDatastoreSession;
import com.buschmais.xo.neo4j.api.Neo4jXOProvider;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.tooling.GlobalGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Store} implementation using an embedded Neo4j instance.
 */
public class EmbeddedGraphStore extends AbstractGraphStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedGraphStore.class);

    private static final String PROPERTY_NEO4J_ALLOW_STORE_UPGRADE = "neo4j.allow_store_upgrade";
    private static final String PROPERTY_NEO4J_KEEP_LOGICAL_LOGS = "neo4j.keep_logical_logs";
    private static final int BATCH_LIMIT = 4096;

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
        XOUnit xoUnit = XOUnitBuilder.create(database.toURI(), Neo4jXOProvider.class, types.toArray(new Class<?>[0]))
                .property(PROPERTY_NEO4J_ALLOW_STORE_UPGRADE, Boolean.TRUE.toString()).property(PROPERTY_NEO4J_KEEP_LOGICAL_LOGS, Boolean.FALSE.toString())
                .create();
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
        long totalNodes = 0;
        Map<String,Object> params = new HashMap<>();
        params.put("limit", BATCH_LIMIT);
        do {
            beginTransaction();
            nodes = executeQuery("MATCH (n) WITH n LIMIT {limit} DETACH DELETE n RETURN count(n) as nodes", params).getSingleResult().get("nodes", Long.class);
            commitTransaction();
            totalNodes += nodes;
        } while (nodes == BATCH_LIMIT);
        LOGGER.info("Reset finished (removed " + totalNodes + " nodes.)");
    }


}
