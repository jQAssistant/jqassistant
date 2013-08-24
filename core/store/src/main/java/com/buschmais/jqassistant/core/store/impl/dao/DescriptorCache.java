package com.buschmais.jqassistant.core.store.impl.dao;

import com.buschmais.jqassistant.core.model.api.descriptor.Descriptor;
import org.apache.commons.collections.map.LRUMap;

import java.util.HashMap;
import java.util.Map;

public class DescriptorCache {

    private final Map<String, Descriptor> cache = new HashMap<>();
    private final Map<String, Descriptor> sharedCache = new LRUMap(10240000);

    public <T extends Descriptor> void put(T descriptor) {
        if (this.cache.put(descriptor.getFullQualifiedName(), descriptor) != null) {
            throw new IllegalStateException("Cannot put two instances with same FQN into cache: " + descriptor.getFullQualifiedName());
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Descriptor> T findBy(String fullQualifiedName) {
        T descriptor = (T) cache.get(fullQualifiedName);
        if (descriptor != null) {
            return descriptor;
        }
        descriptor = (T) sharedCache.get(fullQualifiedName);
        if (descriptor != null) {
            cache.put(fullQualifiedName, descriptor);
        }
        return descriptor;
    }

    public Iterable<Descriptor> getDescriptors() {
        return cache.values();
    }

    public void flush() {
        this.sharedCache.putAll(cache);
        this.cache.clear();
    }
}
