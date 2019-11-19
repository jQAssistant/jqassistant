package com.buschmais.jqassistant.neo4j.backend.bootstrap;

import java.util.Collection;

import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Abstract base class for {@link EmbeddedNeo4jServer}s.
 */
public abstract class AbstractEmbeddedNeo4jServer implements EmbeddedNeo4jServer {

    protected GraphDatabaseService graphDatabaseService;

    protected EmbeddedNeo4jConfiguration embeddedNeo4jConfiguration;

    @Override
    public final void initialize(GraphDatabaseService graphDatabaseService, EmbeddedNeo4jConfiguration configuration, Collection<Class<?>> procedureTypes,
            Collection<Class<?>> functionTypes) {
        this.graphDatabaseService = graphDatabaseService;
        this.embeddedNeo4jConfiguration = configuration;
        initialize(procedureTypes, functionTypes);
    }

    @Override
    public GraphDatabaseService getGraphDatabaseService() {
        return graphDatabaseService;
    }

    /**
     * Configure the {@link GraphDatabaseService} instances.
     *
     * @param procedureTypes
     *            The procedures to register.
     * @param functionTypes
     *            The functions to register.
     */
    protected abstract void initialize(Collection<Class<?>> procedureTypes, Collection<Class<?>> functionTypes);

}
