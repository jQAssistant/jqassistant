package com.buschmais.jqassistant.core.store.impl.dao.mapper;

import com.buschmais.jqassistant.core.model.api.descriptor.*;
import com.buschmais.jqassistant.core.model.api.descriptor.value.AnnotationValueDescriptor;
import com.buschmais.jqassistant.core.store.api.model.NodeLabel;
import com.buschmais.jqassistant.core.store.api.model.NodeProperty;
import com.buschmais.jqassistant.core.store.api.model.Relation;
import org.neo4j.graphdb.Label;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.buschmais.jqassistant.core.store.api.model.NodeLabel.FIELD;

/**
 * A mapper for {@link FieldDescriptor}s.
 */
public class FieldDescriptorMapper extends AbstractDescriptorMapper<FieldDescriptor> {

    @Override
    public Set<Class<? extends FieldDescriptor>> getJavaType() {
        Set<Class<? extends FieldDescriptor>> javaTypes = new HashSet<>();
        javaTypes.add(FieldDescriptor.class);
        return javaTypes;
    }

    @Override
    public NodeLabel getCoreLabel() {
        return FIELD;
    }


    @Override
    public FieldDescriptor createInstance(Set<Label> labels) {
        return new FieldDescriptor();
    }

    @Override
    public FieldDescriptor createInstance(Class<? extends FieldDescriptor> type) {
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
                descriptor.getAnnotatedBy().add((AnnotationValueDescriptor) target);
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
        if (descriptor.isSynthetic() != null) {
            properties.put(NodeProperty.SYNTHETIC, descriptor.isSynthetic());
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
                case SYNTHETIC:
                    descriptor.setSynthetic((Boolean) value);
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
