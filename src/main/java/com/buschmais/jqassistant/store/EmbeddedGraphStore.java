package com.buschmais.jqassistant.store;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class EmbeddedGraphStore extends AbstractinVMGraphStore {

    @Override
    protected GraphDatabaseService startDatabase() {
        return new GraphDatabaseFactory().newEmbeddedDatabase("target/graph.db");
    }

    @Override
    protected void stopDatabase(GraphDatabaseService database) {
        database.shutdown();
    }

}
