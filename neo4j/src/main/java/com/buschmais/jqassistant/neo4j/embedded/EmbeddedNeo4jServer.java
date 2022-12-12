package com.buschmais.jqassistant.neo4j.embedded;

import java.util.Collection;

import com.buschmais.jqassistant.neo4j.embedded.configuration.Embedded;
import com.buschmais.xo.neo4j.embedded.impl.datastore.EmbeddedDatastore;

/**
 * Defines the interface for the server providing Neo4j and jQAssistant
 * functionality.
 */
public interface EmbeddedNeo4jServer {

    String getVersion();

    void initialize(EmbeddedDatastore embeddedDatastore, Embedded embedded, ClassLoader classLoader, Collection<Class<?>> procedureTypes,
        Collection<Class<?>> functionTypes);

    void start();

    void stop();
}
