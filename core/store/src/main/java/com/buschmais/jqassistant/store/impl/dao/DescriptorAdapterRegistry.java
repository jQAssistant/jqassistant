package com.buschmais.jqassistant.store.impl.dao;

import com.buschmais.jqassistant.store.api.DescriptorDAO.CoreLabel;
import com.buschmais.jqassistant.store.api.model.AbstractDescriptor;
import com.buschmais.jqassistant.store.impl.dao.mapper.DescriptorMapper;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;

import java.util.HashMap;
import java.util.Map;

public class DescriptorAdapterRegistry {

    private final Map<Class<? extends AbstractDescriptor>, DescriptorMapper<?>> adaptersByJavaType = new HashMap<Class<? extends AbstractDescriptor>, DescriptorMapper<?>>();
    private final Map<String, DescriptorMapper<?>> adaptersByCoreLabel = new HashMap<String, DescriptorMapper<?>>();


    public void registerDAO(DescriptorMapper<? extends AbstractDescriptor> adapter) {
        this.adaptersByJavaType.put(adapter.getJavaType(), adapter);
        this.adaptersByCoreLabel.put(adapter.getCoreLabel().name(), adapter);
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractDescriptor> DescriptorMapper<T> getDescriptorAdapter(Node node) {
        ResourceIterator<Label> labels = node.getLabels().iterator();
        try {
            while (labels.hasNext()) {
                Label label = labels.next();
                DescriptorMapper<T> mapper = (DescriptorMapper<T>) adaptersByCoreLabel.get(label.name());
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
    public <T extends AbstractDescriptor> DescriptorMapper<T> getDescriptorAdapter(Class<?> javaType) {
        DescriptorMapper<T> adapter = (DescriptorMapper<T>) adaptersByJavaType.get(javaType);
        if (adapter == null) {
            throw new IllegalArgumentException("Cannot find mapper for java type " + javaType);
        }
        return adapter;
    }
}
