package com.buschmais.jqassistant.core.store.impl;

import static com.buschmais.xo.api.Query.Result;
import static com.buschmais.xo.api.Query.Result.CompositeRowObject;

import java.util.Collection;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.store.api.descriptor.FullQualifiedNameDescriptor;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.ResultIterable;
import com.buschmais.xo.api.XOManager;
import com.buschmais.xo.api.XOManagerFactory;

/**
 * Abstract base implementation of a {@link Store}.
 * <p>
 * Provides methods for managing the life packages of a store, transactions,
 * resolving descriptors and executing CYPHER queries.
 * </p>
 */
public abstract class AbstractGraphStore implements Store {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGraphStore.class);
    private XOManagerFactory xoManagerFactory;
    private XOManager xoManager;

    @Override
    public void start(Collection<Class<?>> types) {
        xoManagerFactory = createXOManagerFactory(types);
        xoManager = xoManagerFactory.createXOManager();
    }

    @Override
    public void stop() {
        if (xoManager != null) {
            if (xoManager.currentTransaction().isActive()) {
                LOGGER.warn("Rolling back an active transaction.");
                xoManager.currentTransaction().rollback();
            }
            xoManager.close();
        }
        if (xoManagerFactory != null) {
            closeXOManagerFactory(xoManagerFactory);
        }
    }

    @Override
    public <T extends Descriptor> T create(Class<T> type) {
        T descriptor = xoManager.create(type);
        return descriptor;
    }

    @Override
    public <S extends Descriptor, R extends Descriptor, T extends Descriptor> R create(S source, Class<R> relationType, T target) {
        R descriptor = xoManager.create(source, relationType, target);
        return descriptor;
    }

    @Override
    public <T extends FullQualifiedNameDescriptor> T create(Class<T> type, String fullQualifiedName) {
        T descriptor = xoManager.create(type);
        descriptor.setFullQualifiedName(fullQualifiedName);
        return descriptor;
    }

    @Override
    public <T extends Descriptor, C> C migrate(T descriptor, Class<C> concreteType, Class<?>... types) {
        return xoManager.migrate(descriptor, concreteType, types).as(concreteType);
    }

    @Override
    public <T extends Descriptor> T find(Class<T> type, String fullQualifiedName) {
        ResultIterable<T> result = xoManager.find(type, fullQualifiedName);
        return result.hasResult() ? result.getSingleResult() : null;
    }

    @Override
    public Result<CompositeRowObject> executeQuery(String query, Map<String, Object> parameters) {
        return xoManager.createQuery(query).withParameters(parameters).execute();
    }

    @Override
    public <Q> Result<Q> executeQuery(Class<Q> query, Map<String, Object> parameters) {
        return xoManager.createQuery(query).withParameters(parameters).execute();
    }

    @Override
    public void reset() {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("Resetting store.");
        runQueryUntilResultIsZero("MATCH (n)-[r]-() WITH n, collect(r) as rels LIMIT 10000 FOREACH (r in rels | DELETE r) DELETE n RETURN COUNT(*) as deleted");
        runQueryUntilResultIsZero("MATCH (n) WITH n LIMIT 50000 DELETE n RETURN COUNT(*) as deleted");
        if (LOGGER.isInfoEnabled())
            LOGGER.info("Reset finished.");
    }

    private void runQueryUntilResultIsZero(String deleteNodesAndRels) {
        boolean hasResult;
        do {
            beginTransaction();
            Query<Long> deleteNodesAndRelQuery = xoManager.createQuery(deleteNodesAndRels, Long.class);
            hasResult = deleteNodesAndRelQuery.execute().getSingleResult() > 0;
            commitTransaction();
        } while (hasResult);
    }

    @Override
    public void beginTransaction() {
        xoManager.currentTransaction().begin();
    }

    @Override
    public void commitTransaction() {
        xoManager.currentTransaction().commit();
    }

    @Override
    public void rollbackTransaction() {
        xoManager.currentTransaction().rollback();
    }

    public GraphDatabaseAPI getDatabaseService() {
        beginTransaction();
        try {
            return getDatabaseAPI(xoManager);
        } finally {
            commitTransaction();
        }
    }

    protected abstract GraphDatabaseAPI getDatabaseAPI(XOManager cdoManager);

    /**
     * Delegates to the sub class to start the database.
     * 
     * @return The {@link GraphDatabaseService} instance to use.
     */
    protected abstract XOManagerFactory createXOManagerFactory(Collection<Class<?>> types);

    /**
     * Delegates to the sub class to stop the database.
     * 
     * @param database
     *            The used {@link GraphDatabaseService} instance.
     */
    protected abstract void closeXOManagerFactory(XOManagerFactory database);

}
