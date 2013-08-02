package com.buschmais.jqassistant.store.impl.dao;

import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.core.model.api.descriptor.AbstractDescriptor;

public class DescriptorCache {

    private final Map<Long, AbstractDescriptor> cache = new HashMap<Long, AbstractDescriptor>();

    public <T extends AbstractDescriptor> void put(T descriptor) {
        Long key = Long.valueOf(descriptor.getId());
        this.cache.put(key, descriptor);
    }

	@SuppressWarnings("unchecked")
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
