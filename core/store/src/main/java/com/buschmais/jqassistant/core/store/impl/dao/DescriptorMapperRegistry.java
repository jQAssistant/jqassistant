package com.buschmais.jqassistant.core.store.impl.dao;

import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.store.impl.dao.mapper.DescriptorMapper;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import java.util.Collection;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds all registered {@link DescriptorMapper}s.
 */
public class DescriptorMapperRegistry {

    private final Map<Class<? extends Descriptor>, DescriptorMapper<?>> mappersByJavaType = new HashMap<>();
    private final Map<String, DescriptorMapper<?>> mappersByCoreLabel = new HashMap<>();

    /**
     * Register a store.
     *
     * @param mapper The Mapper.
     */
    public void register(DescriptorMapper<? extends Descriptor> mapper) {
        for (Class<? extends Descriptor> javaType : mapper.getJavaType()) {
            this.mappersByJavaType.put(javaType, mapper);
        }
        this.mappersByCoreLabel.put(mapper.getPrimaryLabel().name(), mapper);
    }

    /**
     * Return the store for a given node.
     *
     * @param node The node.
     * @param <T>  The descriptor types.
     * @return The store.
     */
    @SuppressWarnings("unchecked")
    public <T extends Descriptor> DescriptorMapper<T> getDescriptorMapper(Node node) {
        ResourceIterator<Label> labels = node.getLabels().iterator();
        try {
            while (labels.hasNext()) {
                Label label = labels.next();
                DescriptorMapper<T> mapper = (DescriptorMapper<T>) mappersByCoreLabel.get(label.name());
                if (mapper != null) {
                    return mapper;
                }
            }
        } finally {
            labels.close();
        }
        return null;
    }

    /**
     * Return the store for the given java types.
     *
     * @param javaType The java types.
     * @param <T>      The descriptor types.
     * @return The store.
     */
    @SuppressWarnings("unchecked")
    public <T extends Descriptor> DescriptorMapper<T> getDescriptorMapper(Class<?> javaType) {
        DescriptorMapper<T> mapper = (DescriptorMapper<T>) mappersByJavaType.get(javaType);
        if (mapper == null) {
            throw new IllegalArgumentException("Cannot find mapper for java type " + javaType);
        }
        return mapper;
    }

    public Collection<DescriptorMapper<?>> getDescriptorMappers() {
        return mappersByCoreLabel.values();
    }
}
