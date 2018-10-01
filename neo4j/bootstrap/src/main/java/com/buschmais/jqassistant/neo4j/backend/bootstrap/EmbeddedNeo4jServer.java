package com.buschmais.jqassistant.neo4j.backend.bootstrap;

import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Defines the interface for the server providing Neo4j and jQAssistant
 * functionality.
 */
public interface EmbeddedNeo4jServer {

    String DEFAULT_ADDRESS = "localhost";

    int DEFAULT_PORT = 7474;

    String getVersion();

    void init(GraphDatabaseService graphDatabaseService, boolean apocEnabled);

    GraphDatabaseService getGraphDatabaseService();

    void start(String bindAddress, int httpPort);

    void stop();

}
