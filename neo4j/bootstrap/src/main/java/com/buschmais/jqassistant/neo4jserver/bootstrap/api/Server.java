package com.buschmais.jqassistant.neo4jserver.bootstrap.api;

/**
 * Defines the interface for the server providing Neo4j and jQAssistant
 * functionality.
 */
public interface Server {

    String DEFAULT_ADDRESS = "localhost";

    int DEFAULT_PORT = 7474;

    void start();

    void stop();

}
