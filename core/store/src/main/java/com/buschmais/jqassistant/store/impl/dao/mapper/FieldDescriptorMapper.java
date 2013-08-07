package com.buschmais.jqassistant.store.impl.dao.mapper;

import com.buschmais.jqassistant.core.model.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.FieldDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.VisibilityModifier;
import com.buschmais.jqassistant.store.api.model.NodeLabel;
import com.buschmais.jqassistant.store.api.model.NodeProperty;
import com.buschmais.jqassistant.store.api.model.Relation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A mapper for {@link FieldDescriptor}s.
 */
public class FieldDescriptorMapper extends AbstractDescriptorMapper<FieldDescriptor> {

    @Override
    public Class<FieldDescriptor> getJavaType() {
        return FieldDescriptor.class;
    }

    @Override
    public NodeLabel getCoreLabel() {
        return NodeLabel.FIELD;
    }

    @Override
    public FieldDescriptor createInstance() {
        return new FieldDescriptor();
    }

    @Override
    public Map<Relation, Set<? extends Descriptor>> getRelations(FieldDescriptor descriptor) {
        Map<Relation, Set<? extends Descriptor>> relations = new HashMap<Relation, Set<? extends Descriptor>>();
        relations.put(Relation.ANNOTATED_BY, descriptor.getAnnotatedBy());
        relations.put(Relation.DEPENDS_ON, descriptor.getDependencies());
        return relations;
    }

    @Override
    protected void setRelation(FieldDescriptor descriptor, Relation relation, Descriptor target) {
        switch (relation) {
            case ANNOTATED_BY:
                descriptor.getAnnotatedBy().add((TypeDescriptor) target);
                break;
            case DEPENDS_ON:
                descriptor.getDependencies().add((TypeDescriptor) target);
                break;
            default:
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<NodeProperty, Object> getProperties(FieldDescriptor descriptor) {
        Map<NodeProperty, Object> properties = super.getProperties(descriptor);
        if (descriptor.getVisibility() != null) {
            properties.put(NodeProperty.VISIBILITY, descriptor.getVisibility().name());
        }
        if (descriptor.isStatic() != null) {
            properties.put(NodeProperty.STATIC, descriptor.isStatic());
        }
        if (descriptor.isFinal() != null) {
            properties.put(NodeProperty.FINAL, descriptor.isFinal());
        }
        if (descriptor.isVolatile() != null) {
            properties.put(NodeProperty.VOLATILE, descriptor.isVolatile());
        }
        if (descriptor.isTransient() != null) {
            properties.put(NodeProperty.TRANSIENT, descriptor.isTransient());
        }
        return properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProperty(FieldDescriptor descriptor, NodeProperty property, Object value) {
        if (value != null) {
            super.setProperty(descriptor, property, value);
            switch (property) {
                case STATIC:
                    descriptor.setStatic((Boolean) value);
                    break;
                case FINAL:
                    descriptor.setFinal((Boolean) value);
                    break;
                case VOLATILE:
                    descriptor.setVolatile((Boolean) value);
                    break;
                case TRANSIENT:
                    descriptor.setTransient((Boolean) value);
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
