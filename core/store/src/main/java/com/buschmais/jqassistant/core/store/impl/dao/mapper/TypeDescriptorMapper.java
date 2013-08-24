package com.buschmais.jqassistant.core.store.impl.dao.mapper;

import com.buschmais.jqassistant.core.model.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.JavaType;
import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.VisibilityModifier;
import com.buschmais.jqassistant.core.model.api.descriptor.value.AnnotationValueDescriptor;
import com.buschmais.jqassistant.core.store.api.model.NodeLabel;
import com.buschmais.jqassistant.core.store.api.model.NodeProperty;
import com.buschmais.jqassistant.core.store.api.model.Relation;
import org.neo4j.graphdb.Label;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.buschmais.jqassistant.core.store.api.model.NodeLabel.TYPE;

/**
 * A mapper for {@link TypeDescriptor}s.
 */
public class TypeDescriptorMapper extends AbstractDescriptorMapper<TypeDescriptor> {

    @Override
    public Set<Class<? extends TypeDescriptor>> getJavaType() {
        Set<Class<? extends TypeDescriptor>> javaTypes = new HashSet<>();
        javaTypes.add(TypeDescriptor.class);
        return javaTypes;
    }

    @Override
    public NodeLabel getCoreLabel() {
        return TYPE;
    }

    @Override
    public Class<? extends TypeDescriptor> getType(Set<Label> labels) {
        return TypeDescriptor.class;
    }

    @Override
    public TypeDescriptor createInstance(Class<? extends TypeDescriptor> type) {
        return new TypeDescriptor();
    }

    @Override
    public Map<Relation, Set<? extends Descriptor>> getRelations(TypeDescriptor descriptor) {
        Map<Relation, Set<? extends Descriptor>> relations = new HashMap<>();
        relations.put(Relation.ANNOTATED_BY, descriptor.getAnnotatedBy());
        relations.put(Relation.CONTAINS, descriptor.getContains());
        relations.put(Relation.DEPENDS_ON, descriptor.getDependencies());
        relations.put(Relation.IMPLEMENTS, descriptor.getInterfaces());
        relations.put(Relation.EXTENDS, asSet(descriptor.getSuperClass()));
        return relations;
    }

    @Override
    protected void setRelation(TypeDescriptor descriptor, Relation relation, Descriptor target) {
        switch (relation) {
            case ANNOTATED_BY:
                descriptor.getAnnotatedBy().add((AnnotationValueDescriptor) target);
                break;
            case CONTAINS:
                descriptor.getContains().add(target);
                break;
            case DEPENDS_ON:
                descriptor.getDependencies().add((TypeDescriptor) target);
                break;
            case IMPLEMENTS:
                descriptor.getInterfaces().add((TypeDescriptor) target);
                break;
            case EXTENDS:
                descriptor.setSuperClass((TypeDescriptor) target);
                break;
            default:
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<NodeProperty, Object> getProperties(TypeDescriptor descriptor) {
        Map<NodeProperty, Object> properties = super.getProperties(descriptor);
        if (descriptor.isAbstract() != null) {
            properties.put(NodeProperty.ABSTRACT, descriptor.isAbstract());
        }
        if (descriptor.getVisibility() != null) {
            properties.put(NodeProperty.VISIBILITY, descriptor.getVisibility().name());
        }
        if (descriptor.isStatic() != null) {
            properties.put(NodeProperty.STATIC, descriptor.isStatic());
        }
        if (descriptor.isFinal() != null) {
            properties.put(NodeProperty.FINAL, descriptor.isFinal());
        }
        if (descriptor.isSynthetic() != null) {
            properties.put(NodeProperty.SYNTHETIC, descriptor.isSynthetic());
        }
        return properties;
    }

    @Override
    public void setProperty(TypeDescriptor descriptor, NodeProperty property, Object value) {
        if (value != null) {
            super.setProperty(descriptor, property, value);
            switch (property) {
                case ABSTRACT:
                    descriptor.setAbstract((Boolean) value);
                    break;
                case STATIC:
                    descriptor.setStatic((Boolean) value);
                    break;
                case FINAL:
                    descriptor.setFinal((Boolean) value);
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

    @Override
    public Set<Label> getLabels(TypeDescriptor descriptor) {
        Set<Label> labels = new HashSet<>();
        final JavaType javaType = descriptor.getJavaType();
        if (javaType != null) {
            labels.add(new com.buschmais.jqassistant.core.store.impl.dao.mapper.Label(javaType));
        }
        return labels;
    }

    @Override
    public void setLabel(TypeDescriptor descriptor, Label label) {
        JavaType javaType = JavaType.getJavaType(label.name());
        if (javaType != null) {
            descriptor.setJavaType(javaType);
        }
    }
}
