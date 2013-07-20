package com.buschmais.jqassistant.store.impl.dao;

import com.buschmais.jqassistant.store.api.model.AbstractDescriptor;
import org.neo4j.graphdb.Node;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class DescriptorCache {

    private final Map<Long, AbstractDescriptor> cache = new HashMap<Long, AbstractDescriptor>();

    public <T extends AbstractDescriptor> void put(T descriptor) {
        Long key = Long.valueOf(descriptor.getId());
        this.cache.put(key, descriptor);
    }

    public <T extends AbstractDescriptor> T findBy(Long id) {
        return (T) cache.get(id);
    }

    public Iterable<AbstractDescriptor> getDescriptors() {
        return cache.values();
    }

    public void clear() {
        this.cache.clear();
    }
}
