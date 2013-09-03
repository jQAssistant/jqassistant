package com.buschmais.jqassistant.core.store.impl.dao;

import com.buschmais.jqassistant.core.model.api.descriptor.Descriptor;
import org.apache.commons.collections.map.LRUMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Provides caching functionality for descriptors and indexes.
 */
public class DescriptorCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(DescriptorCache.class);

    /**
     * The transactional index cache.
     */
    private final Map<String, Long> indexCache = new HashMap<>();
    /**
     * The shared index cache (limited size).
     */
    private final Map<String, Long> sharedIndexCache = new LRUMap(65536);

    /**
     * The transactional descriptor cache.
     */
    private final Map<Long, Descriptor> cache = new HashMap<>();

    /**
     * The reference cache (weak references).
     */
    private final Map<Long, SoftReference<Descriptor>> referenceCache = new WeakHashMap<>();

    /**
     * The shared cache (limited size).
     */
    private final Map<Long, Descriptor> sharedCache = new LRUMap(65536);

    /**
     * Put a descriptor into the cache.
     *
     * @param descriptor The descriptor.
     * @param <T>        The descriptor type.
     */
    public <T extends Descriptor> void put(T descriptor) {
        if (this.cache.put(descriptor.getId(), descriptor) != null) {
            throw new IllegalStateException("Cannot put two instances with same ID into cache: " + descriptor.getId());
        }
        this.indexCache.put(descriptor.getFullQualifiedName(), descriptor.getId());
    }

    /**
     * Find a node/descriptor id using its full qualified name.
     *
     * @param fullQualifiedName The full qualified name.
     * @return The node/descriptor id or <code>null</code>.
     */
    public Long findBy(String fullQualifiedName) {
        Long id = indexCache.get(fullQualifiedName);
        if (id != null) {
            return id;
        }
        id = sharedIndexCache.get(fullQualifiedName);
        if (id != null) {
            indexCache.put(fullQualifiedName, id);
        }
        return id;
    }

    /**
     * Find a descriptor using its id.
     *
     * @param id  The id.
     * @param <T> The descriptor type.
     * @return The descriptor or <code>null</code>.
     */
    public <T extends Descriptor> T findBy(Long id) {
        T descriptor = (T) cache.get(id);
        if (descriptor != null) {
            LOGGER.trace("Hit '{}' (TX).", id);
            return descriptor;
        }
        SoftReference<Descriptor> reference = referenceCache.get(id);
        if (reference != null) {
            descriptor = (T) reference.get();
        }
        if (descriptor == null) {
            descriptor = (T) sharedCache.get(id);
        }
        if (descriptor != null) {
            LOGGER.trace("Hit '{}' (Ref/Shared).", id);
            cache.put(id, descriptor);
        } else {
            LOGGER.trace("Miss '{}'.", id);
        }
        return descriptor;
    }

    /**
     * Get all descriptors from the transactional descriptor cache.
     * @return The descriptors.
     */
    public Iterable<Descriptor> getDescriptors() {
        return cache.values();
    }

    /**
     * Flush all transactional
     */
    public void flush() {
        for (Map.Entry<Long, Descriptor> entry : cache.entrySet()) {
            this.referenceCache.put(entry.getKey(), new SoftReference<>(entry.getValue()));
            this.sharedCache.put(entry.getKey(), entry.getValue());
        }
        LOGGER.trace("ref='{}', shared '{}'.", this.referenceCache.size(), this.sharedCache.size());
        this.cache.clear();
        for (Map.Entry<String, Long> entry : indexCache.entrySet()) {
            this.sharedIndexCache.put(entry.getKey(), entry.getValue());
        }
        LOGGER.trace("sharedIndex '{}'.", this.sharedIndexCache.size());
        this.indexCache.clear();
    }
}
