package com.buschmais.jqassistant.store.impl;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.server.WrappingNeoServerBootstrapper;
import org.neo4j.tooling.GlobalGraphOperations;

import com.buschmais.jqassistant.store.api.Store;

public abstract class AbstractGraphStore implements Store {

    protected GraphDatabaseService database;
    private WrappingNeoServerBootstrapper server;

    @Override
    public void start() {
        database = startDatabase();
        beginTransaction();
        for (Relationship relationShip : GlobalGraphOperations.at(database).getAllRelationships()) {
            relationShip.delete();
        }
        for (Node node : GlobalGraphOperations.at(database).getAllNodes()) {
            node.delete();
        }
        endTransaction();
        server = new WrappingNeoServerBootstrapper((GraphDatabaseAPI) database);
        server.start();
    }

    @Override
    public void stop() {
        server.stop();
        stopDatabase(database);
    }

    protected abstract GraphDatabaseService startDatabase();

    protected abstract void stopDatabase(GraphDatabaseService database);

}