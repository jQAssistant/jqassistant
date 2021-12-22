package com.buschmais.jqassistant.neo4j.backend.bootstrap;

import java.util.Collection;

import com.buschmais.xo.neo4j.embedded.impl.datastore.EmbeddedDatastore;

/**
 * Abstract base class for {@link EmbeddedNeo4jServer}s.
 */
public abstract class AbstractEmbeddedNeo4jServer implements EmbeddedNeo4jServer {

    protected EmbeddedDatastore embeddedDatastore;

    protected EmbeddedNeo4jConfiguration embeddedNeo4jConfiguration;

    @Override
    public final void initialize(EmbeddedDatastore embeddedDatastore, EmbeddedNeo4jConfiguration configuration, Collection<Class<?>> procedureTypes,
        Collection<Class<?>> functionTypes) {
        this.embeddedDatastore = embeddedDatastore;
        this.embeddedNeo4jConfiguration = configuration;
        initialize(procedureTypes, functionTypes);
    }

    /**
     * Initialize the embedded server.
     *
     * @param procedureTypes
     *     The procedures to register.
     * @param functionTypes
     *     The functions to register.
     */
    protected abstract void initialize(Collection<Class<?>> procedureTypes, Collection<Class<?>> functionTypes);

}
