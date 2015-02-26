package com.buschmais.jqassistant.core.store.impl;

import static com.buschmais.xo.api.Query.Result;
import static com.buschmais.xo.api.Query.Result.CompositeRowObject;

import java.util.Collection;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
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
    private static final int AUTOCOMMIT_THRESHOLD = 32678;
    private XOManagerFactory xoManagerFactory;
    private XOManager xoManager;
    private int created;

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
        autoCommit();
        return descriptor;
    }

    @Override
    public <S extends Descriptor, R extends Descriptor, T extends Descriptor> R create(S source, Class<R> relationType, T target) {
        R descriptor = xoManager.create(source, relationType, target);
        return descriptor;
    }

    /**
     * Verifies if the auto commit threshold has been reached. If yes the
     * current transaction is committed and a new one started.
     */
    private void autoCommit() {
        created++;
        if (created == AUTOCOMMIT_THRESHOLD) {
            commitTransaction();
            beginTransaction();
        }
    }

    @Override
    public <T extends FullQualifiedNameDescriptor> T create(Class<T> type, String fullQualifiedName) {
        T descriptor = create(type);
        descriptor.setFullQualifiedName(fullQualifiedName);
        return descriptor;
    }

    @Override
    public <T extends Descriptor> void delete(T descriptor) {
        xoManager.delete(descriptor);
    }

    @Override
    public <T extends Descriptor, C> C migrate(T descriptor, Class<C> concreteType, Class<?>... types) {
        return xoManager.migrate(descriptor, concreteType, types).as(concreteType);
    }

    @Override
    public <T extends Descriptor, N extends Descriptor> N addDescriptorType(T descriptor, Class<?> newDescriptorType, Class<N> as) {
        return xoManager.migrate(descriptor).add(newDescriptorType).as(as);
    }

    @Override
    public <T extends Descriptor, N extends Descriptor> N addDescriptorType(T descriptor, Class<N> newDescriptorType) {
        return xoManager.migrate(descriptor).add(newDescriptorType).as(newDescriptorType);
    }

    @Override
    public <T extends Descriptor, N extends Descriptor> N removeDescriptorType(T descriptor, Class<?> obsoleteDescriptorType, Class<N> as) {
        return xoManager.migrate(descriptor).remove(obsoleteDescriptorType).as(as);
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
    public Result<CompositeRowObject> executeQuery(String query) {
        return xoManager.createQuery(query).execute();
    }

    @Override
    public void beginTransaction() {
        xoManager.currentTransaction().begin();
        created = 0;
    }

    @Override
    public void commitTransaction() {
        xoManager.currentTransaction().commit();
    }

    @Override
    public void rollbackTransaction() {
        xoManager.currentTransaction().rollback();
    }

    @Override
    public GraphDatabaseService getGraphDatabaseService() {
        return getGraphDatabaseService(xoManager);
    }

    /**
     * Return the graph database service wrapped by the given XOManager.
     * 
     * @param xoManager
     *            The XOManager.
     * @return The graph database service instance.
     */
    protected abstract GraphDatabaseService getGraphDatabaseService(XOManager xoManager);

    /**
     * Delegates to the sub class to start the database.
     * 
     * @return The {@link GraphDatabaseService} instance to use.
     */
    protected abstract XOManagerFactory createXOManagerFactory(Collection<Class<?>> types);

    /**
     * Delegates to the sub class to stop the factory.
     * 
     * @param factory
     *            The used {@link GraphDatabaseService} instance.
     */
    protected abstract void closeXOManagerFactory(XOManagerFactory factory);

}
