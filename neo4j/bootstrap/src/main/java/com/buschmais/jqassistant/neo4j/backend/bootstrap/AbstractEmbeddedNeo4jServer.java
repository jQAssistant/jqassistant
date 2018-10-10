package com.buschmais.jqassistant.neo4j.backend.bootstrap;

import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Abstract base class for {@link EmbeddedNeo4jServer}s.
 */
public abstract class AbstractEmbeddedNeo4jServer implements EmbeddedNeo4jServer {

    protected GraphDatabaseService graphDatabaseService;

    protected EmbeddedNeo4jConfiguration embeddedNeo4jConfiguration;

    @Override
    public final void initialize(GraphDatabaseService graphDatabaseService, EmbeddedNeo4jConfiguration configuration) {
        this.graphDatabaseService = graphDatabaseService;
        this.embeddedNeo4jConfiguration = configuration;
        initialize();
    }

    @Override
    public GraphDatabaseService getGraphDatabaseService() {
        return graphDatabaseService;
    }

    /**
     * Configure the {@link GraphDatabaseService} instances.
     */
    protected abstract void initialize();

}
