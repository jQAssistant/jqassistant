package com.buschmais.jqassistant.core.store.impl;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.StoreConfiguration;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.core.store.spi.StorePluginRepository;
import com.buschmais.xo.api.*;
import com.buschmais.xo.api.Query.Result;
import com.buschmais.xo.api.bootstrap.XO;
import com.buschmais.xo.api.bootstrap.XOUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Optional.ofNullable;

/**
 * Abstract base implementation of a {@link Store}.
 *
 * Provides methods for managing the life packages of a store, transactions,
 * resolving descriptors and executing CYPHER queries.
 */
public abstract class AbstractGraphStore implements Store {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGraphStore.class);

    private Map<String, Cache<?, ? extends Descriptor>> caches = new HashMap<>();

    protected final StoreConfiguration storeConfiguration;

    protected final StorePluginRepository storePluginRepository;

    private XOManagerFactory xoManagerFactory;
    private XOManager xoManager;
    private int created;

    protected AbstractGraphStore(StoreConfiguration configuration, StorePluginRepository storePluginRepository) {
        this.storeConfiguration = configuration;
        this.storePluginRepository = storePluginRepository;
    }

    @Override
    public void start() {
        XOUnit.XOUnitBuilder builder = XOUnit.builder()
            .uri(storeConfiguration.getUri())
            .classLoader(ofNullable(storePluginRepository.getClassLoader()))
            .types(storePluginRepository.getDescriptorTypes())
            .validationMode(ValidationMode.NONE)
            .clearAfterCompletion(false)
            .mappingConfiguration(XOUnit.MappingConfiguration.builder()
                .strictValidation(true)
                .build());
        configure(builder, storeConfiguration);
        xoManagerFactory = XO.createXOManagerFactory(builder.build());
        initialize(xoManagerFactory);
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
            xoManagerFactory.close();
        }
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
        R descriptor = xoManager.create(source, relationType, target);
        return descriptor;
    }

    @Override
    public <S extends Descriptor, R extends Descriptor, T extends Descriptor> R create(S source, Class<R> relationType, T target, Example<R> example) {
        R descriptor = xoManager.create(source, relationType, target, example);
        return descriptor;
    }

    /**
     * Verifies if the auto commit threshold has been reached. If yes the current
     * transaction is committed and a new one started.
     */
    private void autoCommit() {
        created++;
        if (created == getAutocommitThreshold()) {
            flush();
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
        xoManager.delete(descriptor);
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
    public <T extends Descriptor> T find(Class<T> type, String value) {
        ResultIterable<T> result = xoManager.find(type, value);
        return result.hasResult() ? result.getSingleResult() : null;
    }

    @Override
    public Result<Result.CompositeRowObject> executeQuery(String query, Map<String, Object> parameters) {
        return xoManager.createQuery(query).withParameters(parameters).execute();
    }

    @Override
    public <Q> Result<Q> executeQuery(Class<Q> query, Map<String, Object> parameters) {
        return xoManager.createQuery(query).withParameters(parameters).execute();
    }

    @Override
    public Result<Result.CompositeRowObject> executeQuery(String query) {
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
    public boolean hasActiveTransaction() {
        boolean activeTx = false;

        if (xoManager.currentTransaction() != null && xoManager.currentTransaction().isActive()) {
            activeTx = true;
        }

        return activeTx;
    }

    @Override
    public void reset() {
        LOGGER.info("Resetting store.");
        Map<String, Object> params = new HashMap<>();
        params.put("batchSize", 65536);
        long totalNodes = 0;
        long nodes;
        Instant start = Instant.now();
        do {
            beginTransaction();
            Result.CompositeRowObject result = executeQuery("MATCH (n) " + //
                    "OPTIONAL MATCH (n)-[r]-() " + //
                    "WITH n, r " + //
                    "LIMIT $batchSize " + //
                    "WITH distinct n " + //
                    "DETACH DELETE n " + //
                    "RETURN count(n) as nodes", params).getSingleResult();
            nodes = result.get("nodes", Long.class);
            totalNodes = totalNodes + nodes;
            commitTransaction();
        } while (nodes > 0);
        Instant end = Instant.now();
        LOGGER.info("Reset finished (removed {} nodes, duration: {}s).", totalNodes, Duration.between(start, end).get(ChronoUnit.SECONDS));
    }

    @Override
    public <K, V extends Descriptor> Cache<K, V> getCache(String cacheKey) {
        return (Cache<K, V>) caches.computeIfAbsent(cacheKey, key -> Caffeine.newBuilder().softValues().build());
    }

    @Override
    public void invalidateCache(String cacheKey) {
        caches.remove(cacheKey);
    }

    /**
     * Configure store specific options.
     */
    protected abstract XOUnit configure(XOUnit.XOUnitBuilder builder, StoreConfiguration storeConfiguration);

    /**
     * Initialize the store.
     */
    protected abstract void initialize(XOManagerFactory xoManagerFactory);

    protected abstract int getAutocommitThreshold();

}
