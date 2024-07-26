package com.buschmais.jqassistant.neo4j.embedded;

import java.util.Collection;

import com.buschmais.xo.neo4j.embedded.impl.datastore.EmbeddedDatastore;

/**
 * Defines the interface for the server providing Neo4j and jQAssistant
 * functionality.
 */
public interface EmbeddedNeo4jServer {

    // TODO This method should take com.buschmais.jqassistant.core.store.api.configuration.Embedded as parameter instead of the single values. Requires moving the embedded Neo4j server to the core store module.
    void initialize(EmbeddedDatastore embeddedDatastore, com.buschmais.jqassistant.neo4j.embedded.api.configuration.Server server, String listenAddress,
        Integer httpPort, Integer boltPort, ClassLoader classLoader, Collection<Class<?>> procedureTypes, Collection<Class<?>> functionTypes);

    void start();

    void stop();
}
