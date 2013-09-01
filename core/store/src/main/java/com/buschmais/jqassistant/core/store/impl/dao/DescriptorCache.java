package com.buschmais.jqassistant.core.store.impl.dao;

import com.buschmais.jqassistant.core.model.api.descriptor.Descriptor;
import org.apache.commons.collections.map.LRUMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class DescriptorCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(DescriptorCache.class);

    private final Map<String, WeakReference<Descriptor>> referenceCache = new WeakHashMap<>();
    private final Map<String, Descriptor> cache = new HashMap<>();
    private final Map<String, Descriptor> sharedCache = new LRUMap(512000);

    public <T extends Descriptor> void put(T descriptor) {
        if (this.cache.put(descriptor.getFullQualifiedName(), descriptor) != null) {
            throw new IllegalStateException("Cannot put two instances with same FQN into cache: " + descriptor.getFullQualifiedName());
        }
        LOGGER.trace("Put " + descriptor.getFullQualifiedName());
    }

    @SuppressWarnings("unchecked")
    public <T extends Descriptor> T findBy(String fullQualifiedName) {
        T descriptor = (T) cache.get(fullQualifiedName);
        if (descriptor != null) {
            LOGGER.trace("Hit '{}' (TX).", fullQualifiedName);
            return descriptor;
        }
        WeakReference<Descriptor> weakReference = referenceCache.get(fullQualifiedName);
        if (weakReference != null) {
            descriptor = (T) weakReference.get();
        }
        if (descriptor == null) {
            descriptor = (T) sharedCache.get(fullQualifiedName);
        }
        if (descriptor != null) {
            LOGGER.trace("Hit '{}' (Ref/Shared).", fullQualifiedName);
            cache.put(fullQualifiedName, descriptor);
        } else {
            LOGGER.trace("Miss '{}'.", fullQualifiedName);
        }
        return descriptor;
    }

    public Iterable<Descriptor> getDescriptors() {
        return cache.values();
    }

    public void flush() {
        for (Map.Entry<String, Descriptor> entry : cache.entrySet()) {
            this.referenceCache.put(entry.getKey(), new WeakReference<Descriptor>(entry.getValue()));
            this.sharedCache.put(entry.getKey(), entry.getValue());
        }
        LOGGER.trace("ref='{}', shared '{}'.", this.referenceCache.size(), this.sharedCache.size());
        this.cache.clear();
    }
}
