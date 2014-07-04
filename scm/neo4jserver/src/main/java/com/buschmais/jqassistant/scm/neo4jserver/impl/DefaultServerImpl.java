package com.buschmais.jqassistant.scm.neo4jserver.impl;

import static java.util.Collections.emptyList;

import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;

/**
 * The customized Neo4j server.
 * <p>
 * The class adds the {@link JQAServerModule}
 * </p>
 */
public class DefaultServerImpl extends AbstractServer {

    /**
     * Constructor.
     * 
     * @param graphStore
     *            The store instance to use.
     */
    public DefaultServerImpl(EmbeddedGraphStore graphStore) {
        super(graphStore.getDatabaseService(), graphStore);
    }

    @Override
    protected Iterable<? extends Class<?>> getExtensions() {
        return emptyList();
    }
}
