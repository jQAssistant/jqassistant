package com.buschmais.jqassistant.core.store.impl;

import java.net.URISyntaxException;
import java.util.Collection;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.GraphDatabaseAPI;

import com.buschmais.xo.api.XOManager;
import com.buschmais.xo.api.XOManagerFactory;
import com.buschmais.xo.api.bootstrap.XO;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.api.bootstrap.XOUnitBuilder;
import com.buschmais.xo.neo4j.api.Neo4jXOProvider;

/**
 * {@link com.buschmais.jqassistant.core.store.api.Store} implementation using
 * an existing {@link org.neo4j.graphdb.GraphDatabaseService} instance.
 */
public class GraphDbStore extends AbstractGraphStore {

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
    public GraphDbStore(GraphDatabaseService graphDatabaseService) {
        this.graphDatabaseService = graphDatabaseService;
    }

    @Override
    protected GraphDatabaseAPI getDatabaseAPI(XOManager xoManager) {
        return (GraphDatabaseAPI) graphDatabaseService;
    }

    @Override
    protected XOManagerFactory createXOManagerFactory(Collection<Class<?>> types) {
        XOUnit xoUnit;
        try {
            xoUnit = XOUnitBuilder.create("graphDb:///", Neo4jXOProvider.class, types.toArray(new Class<?>[0]))
                    .property(GraphDatabaseService.class.getName(), graphDatabaseService).create();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Cannot create graph store", e);
        }
        return XO.createXOManagerFactory(xoUnit);
    }

    @Override
    protected void closeXOManagerFactory(XOManagerFactory xoManagerFactory) {
        xoManagerFactory.close();
    }

    @Override
    public void reset() {
        throw new IllegalStateException("Cannot reset graph store");
    }
}
