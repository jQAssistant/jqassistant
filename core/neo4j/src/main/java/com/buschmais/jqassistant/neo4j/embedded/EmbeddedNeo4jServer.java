package com.buschmais.jqassistant.neo4j.embedded;

/**
 * Defines the interface for the server providing Neo4j and jQAssistant
 * functionality.
 */
public interface EmbeddedNeo4jServer {

    // TODO This method should take com.buschmais.jqassistant.core.store.api.configuration.Embedded as parameter instead of the single values. Requires moving the embedded Neo4j server to the core store module.
    void initialize(String listenAddress, Integer httpPort, Integer boltPort, ClassLoader classLoader);

    void start();

    void openBrowser();

    void stop();
}
