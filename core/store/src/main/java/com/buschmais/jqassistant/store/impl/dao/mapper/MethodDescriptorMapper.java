package com.buschmais.jqassistant.store.impl.dao.mapper;

import com.buschmais.jqassistant.core.model.api.descriptor.*;
import com.buschmais.jqassistant.store.api.model.NodeLabel;
import com.buschmais.jqassistant.store.api.model.NodeProperty;
import com.buschmais.jqassistant.store.api.model.Relation;
import org.neo4j.graphdb.Label;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A mapper for {@link MethodDescriptor}s.
 */
public class MethodDescriptorMapper extends AbstractDescriptorMapper<MethodDescriptor> {

    @Override
    public Class<MethodDescriptor> getJavaType() {
        return MethodDescriptor.class;
    }

    @Override
    public NodeLabel getCoreLabel() {
        return NodeLabel.METHOD;
    }

    @Override
    public MethodDescriptor createInstance() {
        return new MethodDescriptor();
    }

    @Override
    public Map<Relation, Set<? extends Descriptor>> getRelations(MethodDescriptor descriptor) {
        Map<Relation, Set<? extends Descriptor>> relations = new HashMap<Relation, Set<? extends Descriptor>>();
        relations.put(Relation.ANNOTATED_BY, descriptor.getAnnotatedBy());
        relations.put(Relation.DEPENDS_ON, descriptor.getDependencies());
        relations.put(Relation.THROWS, descriptor.getDeclaredThrowables());
        return relations;
    }

    @Override
    protected void setRelation(MethodDescriptor descriptor, Relation relation, Descriptor target) {
        switch (relation) {
            case ANNOTATED_BY:
                descriptor.getAnnotatedBy().add((TypeDescriptor) target);
                break;
            case DEPENDS_ON:
                descriptor.getDependencies().add((TypeDescriptor) target);
                break;
            case THROWS:
                descriptor.getDeclaredThrowables().add((TypeDescriptor) target);
                break;
            default:
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Map<NodeProperty, Object> getProperties(MethodDescriptor descriptor) {
        Map<NodeProperty, Object> properties = super.getProperties(descriptor);
        properties.put(NodeProperty.ABSTRACT, descriptor.isAbstract());
        if (descriptor.getVisibility() != null) {
            properties.put(NodeProperty.VISIBILITY, descriptor.getVisibility().name());
        }
        if (descriptor.isStatic() != null) {
            properties.put(NodeProperty.STATIC, descriptor.isStatic());
        }
        if (descriptor.isFinal() != null) {
            properties.put(NodeProperty.FINAL, descriptor.isFinal());
        }
        if (descriptor.isNative() != null) {
            properties.put(NodeProperty.NATIVE, descriptor.isNative());
        }
        return properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProperty(MethodDescriptor descriptor, NodeProperty property, Object value) {
        if (value != null) {
            super.setProperty(descriptor, property, value);
            switch (property) {
                case NATIVE:
                    descriptor.setNative((Boolean) value);
                    break;
                case ABSTRACT:
                    descriptor.setAbstract((Boolean) value);
                    break;
                case STATIC:
                    descriptor.setStatic((Boolean) value);
                    break;
                case FINAL:
                    descriptor.setFinal((Boolean) value);
                    break;
                case VISIBILITY:
                    descriptor.setVisibility(VisibilityModifier.valueOf((String) value));
                    break;
                default:
                    break;
            }
        }
    }
}
