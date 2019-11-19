package com.buschmais.jqassistant.neo4j.backend.bootstrap;

import java.util.Collection;

import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Defines the interface for the server providing Neo4j and jQAssistant
 * functionality.
 */
public interface EmbeddedNeo4jServer {

    String getVersion();

    void initialize(GraphDatabaseService graphDatabaseService, EmbeddedNeo4jConfiguration configuration, Collection<Class<?>> procedureTypes,
            Collection<Class<?>> functionTypes);

    GraphDatabaseService getGraphDatabaseService();

    void start();

    void stop();

}
