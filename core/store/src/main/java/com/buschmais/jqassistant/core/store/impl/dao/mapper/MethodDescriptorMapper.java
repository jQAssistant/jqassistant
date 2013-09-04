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

import static com.buschmais.jqassistant.core.store.api.model.NodeLabel.METHOD;

/**
 * A mapper for {@link MethodDescriptor}s.
 */
public class MethodDescriptorMapper extends AbstractDescriptorMapper<MethodDescriptor> {

    @Override
    public Set<Class<? extends MethodDescriptor>> getJavaType() {
        Set<Class<? extends MethodDescriptor>> javaTypes = new HashSet<>();
        javaTypes.add(MethodDescriptor.class);
        return javaTypes;
    }

    @Override
    public NodeLabel getCoreLabel() {
        return METHOD;
    }

    @Override
    public Class<? extends MethodDescriptor> getType(Set<Label> labels) {
        return MethodDescriptor.class;
    }

    @Override
    public MethodDescriptor createInstance(Class<? extends MethodDescriptor> type) {
        return new MethodDescriptor();
    }

    @Override
    public Map<Relation, Set<? extends Descriptor>> getRelations(MethodDescriptor descriptor) {
        Map<Relation, Set<? extends Descriptor>> relations = new HashMap<Relation, Set<? extends Descriptor>>();
        relations.put(Relation.HAS, descriptor.getParameters());
        relations.put(Relation.THROWS, descriptor.getDeclaredThrowables());
        relations.put(Relation.ANNOTATED_BY, descriptor.getAnnotatedBy());
        relations.put(Relation.DEPENDS_ON, descriptor.getDependencies());
        relations.put(Relation.INVOKES, descriptor.getInvokes());
        relations.put(Relation.READS, descriptor.getReads());
        relations.put(Relation.WRITES, descriptor.getWrites());
        return relations;
    }

    @Override
    protected void setRelation(MethodDescriptor descriptor, Relation relation, Descriptor target) {
        switch (relation) {
            case HAS:
                descriptor.getParameters().add((ParameterDescriptor) target);
                break;
            case THROWS:
                descriptor.getDeclaredThrowables().add((TypeDescriptor) target);
                break;
            case ANNOTATED_BY:
                descriptor.getAnnotatedBy().add((AnnotationValueDescriptor) target);
                break;
            case INVOKES:
                descriptor.getInvokes().add((MethodDescriptor) target);
                break;
            case READS:
                descriptor.getReads().add((FieldDescriptor) target);
                break;
            case WRITES:
                descriptor.getWrites().add((FieldDescriptor) target);
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
    public Map<NodeProperty, Object> getProperties(MethodDescriptor descriptor) {
        Map<NodeProperty, Object> properties = super.getProperties(descriptor);
        properties.put(NodeProperty.SIGNATURE, descriptor.getSignature());
        if (descriptor.getName()!=null) {
            properties.put(NodeProperty.NAME, descriptor.getName());
        }
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
        if (descriptor.isNative() != null) {
            properties.put(NodeProperty.NATIVE, descriptor.isNative());
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
    public void setProperty(MethodDescriptor descriptor, NodeProperty property, Object value) {
        if (value != null) {
            super.setProperty(descriptor, property, value);
            switch (property) {
                case SIGNATURE:
                    descriptor.setSignature((String) value);
                    break;
                case NAME:
                    descriptor.setName((String) value);
                    break;
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
    public Set<Label> getLabels(MethodDescriptor descriptor) {
        Set<Label> labels = new HashSet<>();
        if (Boolean.TRUE.equals(descriptor.isConstructor())) {
            labels.add(NodeLabel.CONSTRUCTOR);
        }
        return labels;
    }

    @Override
    public void setLabel(MethodDescriptor descriptor, Label label) {
        if (NodeLabel.CONSTRUCTOR.name().equals(label.name())) {
            descriptor.setConstructor(Boolean.TRUE);
        }
    }
}
