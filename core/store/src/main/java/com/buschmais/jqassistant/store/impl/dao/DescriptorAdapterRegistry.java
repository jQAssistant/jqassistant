package com.buschmais.jqassistant.store.impl.dao;

import com.buschmais.jqassistant.store.api.model.descriptor.AbstractDescriptor;
import com.buschmais.jqassistant.store.impl.dao.mapper.DescriptorMapper;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;

import java.util.HashMap;
import java.util.Map;

public class DescriptorAdapterRegistry {

    private final Map<Class<? extends AbstractDescriptor>, DescriptorMapper<?>> mappersByJavaType = new HashMap<Class<? extends AbstractDescriptor>, DescriptorMapper<?>>();
    private final Map<String, DescriptorMapper<?>> mappersByCoreLabel = new HashMap<String, DescriptorMapper<?>>();

    public void register(DescriptorMapper<? extends AbstractDescriptor> mapper) {
        this.mappersByJavaType.put(mapper.getJavaType(), mapper);
        this.mappersByCoreLabel.put(mapper.getCoreLabel().name(), mapper);
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractDescriptor> DescriptorMapper<T> getDescriptorMapper(Node node) {
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

    @SuppressWarnings("unchecked")
    public <T extends AbstractDescriptor> DescriptorMapper<T> getDescriptorMapper(Class<?> javaType) {
        DescriptorMapper<T> adapter = (DescriptorMapper<T>) mappersByJavaType.get(javaType);
        if (adapter == null) {
            throw new IllegalArgumentException("Cannot find mapper for java type " + javaType);
        }
        return adapter;
    }
}
