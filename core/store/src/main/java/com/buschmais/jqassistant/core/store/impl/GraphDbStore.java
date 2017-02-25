package com.buschmais.jqassistant.core.store.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import org.neo4j.graphdb.GraphDatabaseService;

import com.buschmais.jqassistant.core.store.api.StoreConfiguration;
import com.buschmais.xo.api.XOManager;
import com.buschmais.xo.api.XOManagerFactory;
import com.buschmais.xo.api.bootstrap.XO;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.api.bootstrap.XOUnitBuilder;
import com.buschmais.xo.neo4j.embedded.api.EmbeddedNeo4jXOProvider;

/**
 * {@link com.buschmais.jqassistant.core.store.api.Store} implementation using
 * an existing {@link org.neo4j.graphdb.GraphDatabaseService} instance.
 */
public class GraphDbStore extends AbstractGraphStore {

    private static final int AUTOCOMMIT_THRESHOLD = 32678;

    /**
     * The {@link org.neo4j.graphdb.GraphDatabaseService}.
     */
    private final GraphDatabaseService graphDatabaseService;

    /**
     * Constructor.
     *
     * @param graphDatabaseService
     *            The {@link org.neo4j.graphdb.GraphDatabaseService}.
     */
    public GraphDbStore(GraphDatabaseService graphDatabaseService) throws URISyntaxException {
        super(StoreConfiguration.builder().uri(new URI("graphDb:///")).build());
        this.graphDatabaseService = graphDatabaseService;
    }

    @Override
    protected GraphDatabaseService getGraphDatabaseService(XOManager xoManager) {
        return graphDatabaseService;
    }

    @Override
    protected XOManagerFactory createXOManagerFactory(Collection<Class<?>> types) {
        XOUnit xoUnit;
        xoUnit = XOUnitBuilder.create(storeConfiguration.getUri(), EmbeddedNeo4jXOProvider.class, types.toArray(new Class<?>[0]))
                .property(GraphDatabaseService.class.getName(), graphDatabaseService).create();
        return XO.createXOManagerFactory(xoUnit);
    }

    @Override
    protected void closeXOManagerFactory(XOManagerFactory xoManagerFactory) {
        xoManagerFactory.close();
    }

    @Override
    protected int getAutocommitThreshold() {
        return AUTOCOMMIT_THRESHOLD;
    }

    @Override
    public void reset() {
        throw new IllegalStateException("Cannot reset graph store");
    }
}
