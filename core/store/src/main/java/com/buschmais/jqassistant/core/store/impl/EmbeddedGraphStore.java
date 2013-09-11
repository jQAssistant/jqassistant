package com.buschmais.jqassistant.core.store.impl;

import com.buschmais.jqassistant.core.store.api.Store;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.tooling.GlobalGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Store} implementation using an embedded Neo4j instance.
 */
public class EmbeddedGraphStore extends AbstractGraphStore {

    /**
     * The directory of the database.
     */
    private final String databaseDirectory;

    /**
     * The current {@link Transaction}.
     */
    private Transaction transaction = null;

    /**
     * Constructor.
     *
     * @param databaseDirectory The directory of the database.
     */
    public EmbeddedGraphStore(String databaseDirectory) {
        this.databaseDirectory = databaseDirectory;
    }

    @Override
    protected GraphDatabaseService startDatabase() {
        return new GraphDatabaseFactory().newEmbeddedDatabase(databaseDirectory);
    }

    @Override
    protected void stopDatabase(GraphDatabaseService database) {
        if (database != null) {
            database.shutdown();
        }
    }

    @Override
    public void beginTransaction() {
        if (transaction != null) {
            throw new IllegalStateException("There is already an existing transaction.");
        }
        transaction = database.beginTx();
    }

    @Override
    public void commitTransaction() {
        if (transaction == null) {
            throw new IllegalStateException("There is no existing transaction.");
        }
        flush();
        transaction.success();
        transaction.finish();
        transaction = null;
    }

    @Override
    public void rollbackTransaction() {
        if (transaction == null) {
            throw new IllegalStateException("There is no existing transaction.");
        }
        transaction.failure();
        transaction.finish();
        transaction = null;
    }

}
