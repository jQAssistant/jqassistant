package com.buschmais.jqassistant.neo4jserver.bootstrap.spi;

import org.neo4j.graphdb.GraphDatabaseService;

public abstract class AbstractServer implements Server {

    protected GraphDatabaseService graphDatabaseService;

    @Override
    public void init(GraphDatabaseService graphDatabaseService) {
        configure(graphDatabaseService);
        this.graphDatabaseService = graphDatabaseService;
    }

    protected abstract void configure(GraphDatabaseService graphDatabaseService);
}
