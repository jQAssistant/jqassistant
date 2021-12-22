package com.buschmais.jqassistant.neo4j.backend.bootstrap;

import java.util.Collection;

import com.buschmais.xo.neo4j.embedded.impl.datastore.EmbeddedDatastore;

/**
 * Defines the interface for the server providing Neo4j and jQAssistant
 * functionality.
 */
public interface EmbeddedNeo4jServer {

    String getVersion();

    void initialize(EmbeddedDatastore embeddedDatastore, EmbeddedNeo4jConfiguration configuration, Collection<Class<?>> procedureTypes,
        Collection<Class<?>> functionTypes);

    void start();

    void stop();

}
