package com.buschmais.jqassistant.core.store.impl.dao;

import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.store.api.descriptor.FullQualifiedNameDescriptor;
import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.iterator.TObjectLongIterator;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import org.apache.commons.collections.map.LRUMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.SoftReference;
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
    private final TObjectLongMap<String> indexCache = new TObjectLongHashMap<>();
    /**
     * The shared index cache (limited size).
     */
    private final Map<String, Long> sharedIndexCache = new LRUMap(65536);

    /**
     * The transactional descriptor cache.
     */
    private final TLongObjectMap<Descriptor> cache = new TLongObjectHashMap<>();

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
        if (this.cache.put(descriptor.getId().longValue(), descriptor) != null) {
            throw new IllegalStateException("Cannot put two instances with same ID into cache: " + descriptor.getId());
        }
    }

    /**
     * Add a descriptor to the index.
     *
     * @param descriptor The descriptor.
     * @param <T>        The descriptor type.
     */
    public <T extends FullQualifiedNameDescriptor> void index(T descriptor) {
        String fullQualifiedName = descriptor.getFullQualifiedName();
        if (fullQualifiedName != null) {
            this.indexCache.put(fullQualifiedName, descriptor.getId().longValue());
        }
    }

    /**
     * Find a node/descriptor id using its full qualified name.
     *
     * @param fullQualifiedName The full qualified name.
     * @return The node/descriptor id or <code>null</code>.
     */
    public Long findBy(String fullQualifiedName) {
        long id = indexCache.get(fullQualifiedName);
        if (id != 0) {
            return Long.valueOf(id);
        }
        Long sharedId = sharedIndexCache.get(fullQualifiedName);
        if (sharedId != null) {
            indexCache.put(fullQualifiedName, sharedId.longValue());
            return sharedId;
        }
        return null;
    }

    /**
     * Find a descriptor using its id.
     *
     * @param id  The id.
     * @param <T> The descriptor type.
     * @return The descriptor or <code>null</code>.
     */
    public <T extends Descriptor> T findBy(Long id) {
        T descriptor = (T) cache.get(id.longValue());
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
            cache.put(id.longValue(), descriptor);
        } else {
            LOGGER.trace("Miss '{}'.", id);
        }
        return descriptor;
    }

    /**
     * Get all descriptors from the transactional descriptor cache.
     *
     * @return The descriptors.
     */
    public Iterable<Descriptor> getDescriptors() {
        return cache.valueCollection();
    }

    /**
     * Flush all transactional
     */
    public void flush() {
        for (TLongObjectIterator<Descriptor> iterator = cache.iterator(); iterator.hasNext(); ) {
            iterator.advance();
            Long key = Long.valueOf(iterator.key());
            Descriptor value = iterator.value();
            this.referenceCache.put(key, new SoftReference<>(value));
            this.sharedCache.put(key, value);
            iterator.remove();
        }
        LOGGER.trace("ref='{}', shared '{}'.", this.referenceCache.size(), this.sharedCache.size());
        for (TObjectLongIterator<String> iterator = indexCache.iterator(); iterator.hasNext(); ) {
            iterator.advance();
            this.sharedIndexCache.put(iterator.key(), Long.valueOf(iterator.value()));
            iterator.remove();
        }
        LOGGER.trace("sharedIndex '{}'.", this.sharedIndexCache.size());
    }
}
