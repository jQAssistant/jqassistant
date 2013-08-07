package com.buschmais.jqassistant.store.impl.dao.mapper;

import com.buschmais.jqassistant.core.model.api.descriptor.AbstractDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.Descriptor;
import com.buschmais.jqassistant.store.api.model.NodeProperty;
import com.buschmais.jqassistant.store.api.model.Relation;
import org.neo4j.graphdb.Label;

import java.util.*;
import java.util.Map.Entry;

/**
 * Abstract base implementation of a {@link DescriptorMapper}.
 *
 * @param <T> The type.
 */
public abstract class AbstractDescriptorMapper<T extends Descriptor> implements DescriptorMapper<T> {

    /**
     * Converts a single descriptor instance to a {@link Set} of descriptors.
     * <p>
     * This is a convenience method.
     * </p>
     *
     * @param descriptor The descriptor.
     * @return The {@link Set} containing the descriptor.
     */
    protected Set<Descriptor> asSet(Descriptor descriptor) {
        if (descriptor != null) {
            Set<Descriptor> set = new HashSet<Descriptor>();
            set.add(descriptor);
            return set;
        }
        return Collections.emptySet();
    }

    @Override
    public Long getId(T descriptor) {
        return descriptor.getId();
    }

    @Override
    public void setId(T descriptor, Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Cannot set id which is null for descriptor " + descriptor);
        }
        descriptor.setId(id);
    }

    @Override
    public void setRelations(T descriptor, Map<Relation, Set<Descriptor>> relations) {
        for (Entry<Relation, Set<Descriptor>> entry : relations.entrySet()) {
            for (Descriptor target : entry.getValue()) {
                setRelation(descriptor, entry.getKey(), target);
            }
        }
    }

    protected abstract void setRelation(T descriptor, Relation relation,Descriptor target);

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<NodeProperty, Object> getProperties(T descriptor) {
        Map<NodeProperty, Object> properties = new HashMap<NodeProperty, Object>();
        properties.put(NodeProperty.FQN, descriptor.getFullQualifiedName());
        return properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProperty(T descriptor, NodeProperty property, Object value) {
        switch (property) {
            case FQN:
                descriptor.setFullQualifiedName((String) value);
                break;
            default:
                break;
        }
    }

    @Override
    public Set<Label> getLabels(T descriptor) {
        return Collections.emptySet();
    }

    @Override
    public void setLabel(T descriptor, Label label) {
    }

}
