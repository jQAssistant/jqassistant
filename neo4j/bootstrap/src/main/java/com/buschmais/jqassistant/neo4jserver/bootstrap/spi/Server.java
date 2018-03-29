package com.buschmais.jqassistant.neo4jserver.bootstrap.spi;

import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Defines the interface for the server providing Neo4j and jQAssistant
 * functionality.
 */
public interface Server {

    String DEFAULT_ADDRESS = "localhost";

    int DEFAULT_PORT = 7474;

    void init(GraphDatabaseService graphDatabaseService);

    void start(String httpAddress, int httpPort);

    void stop();

}
