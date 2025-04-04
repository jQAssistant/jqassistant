package com.buschmais.jqassistant.core.store.impl;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.core.store.spi.StorePluginRepository;
import com.buschmais.xo.api.Example;
import com.buschmais.xo.api.Query.Result;
import com.buschmais.xo.api.ResultIterable;
import com.buschmais.xo.api.ValidationMode;
import com.buschmais.xo.api.XOException;
import com.buschmais.xo.api.XOManager;
import com.buschmais.xo.api.XOManagerFactory;
import com.buschmais.xo.api.bootstrap.XO;
import com.buschmais.xo.api.bootstrap.XOUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Optional.ofNullable;

/**
 * Abstract base implementation of a {@link Store}.
 * <p>
 * Provides methods for managing the life packages of a store, transactions,
 * resolving descriptors and executing CYPHER queries.
 */
public abstract class AbstractGraphStore implements Store {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGraphStore.class);

    private Map<String, Cache<?, ? extends Descriptor>> caches = new HashMap<>();

    protected final URI uri;

    protected final com.buschmais.jqassistant.core.store.api.configuration.Store configuration;

    private final int autoCommitThreshold;

    protected final StorePluginRepository storePluginRepository;

    private XOManagerFactory<?, ?, ?, ?> xoManagerFactory;
    private XOManager xoManager;
    private int created = 0;

    protected AbstractGraphStore(URI uri, com.buschmais.jqassistant.core.store.api.configuration.Store configuration, StorePluginRepository storePluginRepository) {
        this.uri = uri;
        this.configuration = configuration;
        this.autoCommitThreshold = configuration.autoCommitThreshold().orElse(getDefaultAutocommitThreshold());
        this.storePluginRepository = storePluginRepository;
    }

    @Override
    public final void start() {
        XOUnit.XOUnitBuilder builder = XOUnit.builder()
            .uri(uri)
            .classLoader(ofNullable(storePluginRepository.getClassLoader()))
            .types(storePluginRepository.getDescriptorTypes())
            .validationMode(ValidationMode.NONE)
            .clearAfterCompletion(false)
            .mappingConfiguration(XOUnit.MappingConfiguration.builder()
                .strictValidation(true)
                .build());
        configure(builder);
        this.xoManagerFactory = getXOManagerFactory(builder.build());
        initialize(xoManagerFactory);
        xoManager = xoManagerFactory.createXOManager();
    }

    @Override
    public final void stop() {
        if (xoManager != null) {
            if (xoManager.currentTransaction()
                .isActive()) {
                LOGGER.warn("Rolling back an active transaction.");
                xoManager.currentTransaction()
                    .rollback();
            }
            xoManager.close();
        }
        if (xoManagerFactory != null) {
            xoManagerFactory.close();
        }
        destroy();
    }

    @Override
    public XOManager getXOManager() {
        return xoManager;
    }

    @Override
    public <T extends Descriptor> T create(Class<T> type) {
        T descriptor = xoManager.create(type);
        autoCommit();
        return descriptor;
    }

    @Override
    public <T extends Descriptor> T create(Class<T> type, Example<T> example) {
        T descriptor = xoManager.create(type, example);
        autoCommit();
        return descriptor;
    }

    @Override
    public <S extends Descriptor, R extends Descriptor, T extends Descriptor> R create(S source, Class<R> relationType, T target) {
        return xoManager.create(source, relationType, target);
    }

    @Override
    public <S extends Descriptor, R extends Descriptor, T extends Descriptor> R create(S source, Class<R> relationType, T target, Example<R> example) {
        return xoManager.create(source, relationType, target, example);
    }

    /**
     * Verifies if the auto commit threshold has been reached. If yes the current
     * transaction is committed and a new one started.
     */
    private void autoCommit() {
        created++;
        if (created == autoCommitThreshold) {
            flush();
            created = 0;
        }
    }

    @Override
    public void flush() {
        commitTransaction();
        beginTransaction();
    }

    @Override
    public <T extends FullQualifiedNameDescriptor> T create(Class<T> type, String fullQualifiedName) {
        T descriptor = create(type);
        descriptor.setFullQualifiedName(fullQualifiedName);
        return descriptor;
    }

    @Override
    public <T extends Descriptor> void delete(T descriptor) {
        autoCommit();
        xoManager.delete(descriptor);
    }

    @Override
    public <T extends Descriptor, N extends Descriptor> N addDescriptorType(T descriptor, Class<?> newDescriptorType, Class<N> as) {
        return xoManager.migrate(descriptor)
            .add(newDescriptorType)
            .as(as);
    }

    @Override
    public <T extends Descriptor, N extends Descriptor> N addDescriptorType(T descriptor, Class<N> newDescriptorType) {
        return xoManager.migrate(descriptor)
            .add(newDescriptorType)
            .as(newDescriptorType);
    }

    @Override
    public <T extends Descriptor, N extends Descriptor> N removeDescriptorType(T descriptor, Class<?> obsoleteDescriptorType, Class<N> as) {
        return xoManager.migrate(descriptor)
            .remove(obsoleteDescriptorType)
            .as(as);
    }

    @Override
    public <T extends Descriptor> T find(Class<T> type, String value) {
        ResultIterable<T> result = xoManager.find(type, value);
        return result.hasResult() ? result.getSingleResult() : null;
    }

    @Override
    public Result<Result.CompositeRowObject> executeQuery(String query, Map<String, Object> parameters) {
        return xoManager.createQuery(query)
            .withParameters(parameters)
            .execute();
    }

    @Override
    public <Q> Result<Q> executeQuery(Class<Q> query, Map<String, Object> parameters) {
        return xoManager.createQuery(query)
            .withParameters(parameters)
            .execute();
    }

    @Override
    public Result<Result.CompositeRowObject> executeQuery(String query) {
        return xoManager.createQuery(query)
            .execute();
    }

    @Override
    public void beginTransaction() {
        xoManager.currentTransaction()
            .begin();
        created = 0;
    }

    @Override
    public void commitTransaction() {
        xoManager.currentTransaction()
            .commit();
    }

    @Override
    public void rollbackTransaction() {
        xoManager.currentTransaction()
            .rollback();
    }

    @Override
    public boolean hasActiveTransaction() {
        boolean activeTx = false;

        if (xoManager.currentTransaction() != null && xoManager.currentTransaction()
            .isActive()) {
            activeTx = true;
        }

        return activeTx;
    }

    @Override
    public <E extends Exception> void requireTransaction(TransactionalAction<E> transactionalAction) throws E {
        requireTransaction((TransactionalSupplier<Void, E>) () -> {
            transactionalAction.execute();
            return null;
        });
    }

    /**
     * Executes a {@link TransactionalSupplier} within a transaction.
     *
     * @param transactionalSupplier
     *     The {@link TransactionalSupplier}.
     * @param <T>
     *     The return type of the {@link TransactionalSupplier}.
     * @return The value provided by the {@link TransactionalSupplier}.
     * @throws E
     *     If the transaction failed due to an underlying
     *     {@link XOException}.
     */
    @Override
    public <T, E extends Exception> T requireTransaction(TransactionalSupplier<T, E> transactionalSupplier) throws E {
        if (hasActiveTransaction()) {
            return transactionalSupplier.execute();
        }
        try {
            beginTransaction();
            T result = transactionalSupplier.execute();
            commitTransaction();
            return result;
        } finally {
            if (hasActiveTransaction()) {
                rollbackTransaction();
            }
        }
    }

    @Override
    public void reset() {
        LOGGER.info("Resetting store.");
        // clear all caches assigned to that store
        caches.clear();
        Instant start = Instant.now();
        Result.CompositeRowObject result = executeQuery("MATCH (n) " + //
            "CALL { " +  //
            "  WITH n " + //
            "  DETACH DELETE n " + //
            "} IN TRANSACTIONS " + //
            "RETURN count(n) as nodes").getSingleResult();
        long totalNodes = result.get("nodes", Long.class);
        Instant end = Instant.now();
        LOGGER.info("Reset finished (removed {} nodes, duration: {}s).", totalNodes, Duration.between(start, end)
            .get(ChronoUnit.SECONDS));
    }

    @Override
    public <K, V extends Descriptor> Cache<K, V> getCache(String cacheKey) {
        return (Cache<K, V>) caches.computeIfAbsent(cacheKey, key -> Caffeine.newBuilder()
            .softValues()
            .build());
    }

    @Override
    public void invalidateCache(String cacheKey) {
        caches.remove(cacheKey);
    }

    /**
     * May be overwritten for testing.
     *
     * @param xoUnit The {@link XOUnit}.
     * @return The {@link XOManagerFactory}.
     */
    protected XOManagerFactory<?, ?, ?, ?> getXOManagerFactory(XOUnit xoUnit) {
        return XO.createXOManagerFactory(xoUnit);
    }

    /**
     * Configure store specific options.
     */
    protected abstract void configure(XOUnit.XOUnitBuilder builder);

    /**
     * Initialize the store.
     */
    protected abstract void initialize(XOManagerFactory<?, ?, ?, ?> xoManagerFactory);

    protected abstract void destroy();

    protected abstract int getDefaultAutocommitThreshold();

}
