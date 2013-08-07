package com.buschmais.jqassistant.store.impl.dao;

import com.buschmais.jqassistant.core.model.api.descriptor.Descriptor;
import com.buschmais.jqassistant.store.impl.dao.mapper.DescriptorMapper;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds all registered {@link DescriptorMapper}s.
 */
public class DescriptorMapperRegistry {

    private final Map<Class<? extends Descriptor>, DescriptorMapper<?>> mappersByJavaType = new HashMap<Class<? extends Descriptor>, DescriptorMapper<?>>();
    private final Map<String, DescriptorMapper<?>> mappersByCoreLabel = new HashMap<String, DescriptorMapper<?>>();

    /**
     * Register a mapper.
     *
     * @param mapper The Mapper.
     */
    public void register(DescriptorMapper<? extends Descriptor> mapper) {
        this.mappersByJavaType.put(mapper.getJavaType(), mapper);
        this.mappersByCoreLabel.put(mapper.getCoreLabel().name(), mapper);
    }

    /**
     * Return the mapper for a given node.
     *
     * @param node The node.
     * @param <T>  The descriptor type.
     * @return The mapper.
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
     * Return the mapper for the given java type.
     *
     * @param javaType The java type.
     * @param <T>      The descriptor type.
     * @return The mapper.
     */
    @SuppressWarnings("unchecked")
    public <T extends Descriptor> DescriptorMapper<T> getDescriptorMapper(Class<?> javaType) {
        DescriptorMapper<T> mapper = (DescriptorMapper<T>) mappersByJavaType.get(javaType);
        if (mapper == null) {
            throw new IllegalArgumentException("Cannot find mapper for java type " + javaType);
        }
        return mapper;
    }
}
