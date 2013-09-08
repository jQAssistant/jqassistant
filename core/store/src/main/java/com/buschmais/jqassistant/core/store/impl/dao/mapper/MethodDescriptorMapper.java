package com.buschmais.jqassistant.core.store.impl.dao.mapper;

import com.buschmais.jqassistant.core.model.api.descriptor.*;
import com.buschmais.jqassistant.core.model.api.descriptor.value.AnnotationValueDescriptor;
import com.buschmais.jqassistant.core.store.api.model.IndexedLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

import java.util.HashSet;
import java.util.Set;

import static com.buschmais.jqassistant.core.store.impl.dao.mapper.JavaLabel.METHOD;

/**
 * A store for {@link MethodDescriptor}s.
 */
public class MethodDescriptorMapper extends AbstractDescriptorMapper<MethodDescriptor, MethodDescriptorMapper.Property, MethodDescriptorMapper.Relation> {

    enum Property {
        SIGNATURE,
        NAME,
        NATIVE,
        ABSTRACT,
        STATIC,
        FINAL,
        SYNTHETIC,
        VISIBILITY;
    }

    enum Relation implements RelationshipType {
        HAS,
        THROWS,
        ANNOTATED_BY,
        DEPENDS_ON,
        INVOKES,
        READS,
        WRITES;
    }

    @Override
    public Set<Class<? extends MethodDescriptor>> getJavaType() {
        Set<Class<? extends MethodDescriptor>> javaTypes = new HashSet<>();
        javaTypes.add(MethodDescriptor.class);
        return javaTypes;
    }

    @Override
    public IndexedLabel getPrimaryLabel() {
        return METHOD;
    }

    @Override
    protected Class<Property> getPropertyKeys() {
        return Property.class;
    }

    @Override
    protected Class<Relation> getRelationKeys() {
        return Relation.class;
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
    public Set<? extends Descriptor> getRelation(MethodDescriptor descriptor, Relation relation) {
        switch (relation) {
            case HAS:
                return descriptor.getParameters();
            case THROWS:
                return descriptor.getDeclaredThrowables();
            case ANNOTATED_BY:
                return descriptor.getAnnotatedBy();
            case DEPENDS_ON:
                return descriptor.getDependencies();
            case INVOKES:
                return descriptor.getInvokes();
            case READS:
                return descriptor.getReads();
            case WRITES:
                return descriptor.getWrites();
            default:
                break;
        }
        return null;
    }

    @Override
    protected void setRelation(MethodDescriptor descriptor, Relation relation, Set<? extends Descriptor> target) {
        switch (relation) {
            case HAS:
                descriptor.setParameters((Set<ParameterDescriptor>) target);
                break;
            case THROWS:
                descriptor.setDeclaredThrowables((Set<TypeDescriptor>) target);
                break;
            case ANNOTATED_BY:
                descriptor.setAnnotatedBy((Set<AnnotationValueDescriptor>) target);
                break;
            case INVOKES:
                descriptor.setInvokes((Set<MethodDescriptor>) target);
                break;
            case READS:
                descriptor.setReads((Set<FieldDescriptor>) target);
                break;
            case WRITES:
                descriptor.setWrites((Set<FieldDescriptor>) target);
                break;
            case DEPENDS_ON:
                descriptor.setDependencies((Set<TypeDescriptor>) target);
                break;
            default:
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getProperty(MethodDescriptor descriptor, Property property) {
        switch (property) {
            case SIGNATURE:
                return descriptor.getSignature();
            case NAME:
                return descriptor.getName();
            case ABSTRACT:
                return descriptor.isAbstract();
            case VISIBILITY:
                return descriptor.getVisibility() != null ? descriptor.getVisibility().name() : null;
            case STATIC:
                return descriptor.isStatic();
            case FINAL:
                return descriptor.isFinal();
            case NATIVE:
                return descriptor.isNative();
            case SYNTHETIC:
                return descriptor.isSynthetic();
            default:
                break;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProperty(MethodDescriptor descriptor, Property property, Object value) {
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

    @Override
    public Set<? extends Label> getLabels(MethodDescriptor descriptor) {
        Set<Label> labels = new HashSet<>();
        if (Boolean.TRUE.equals(descriptor.isConstructor())) {
            labels.add(JavaLabel.CONSTRUCTOR);
        }
        return labels;
    }

    @Override
    public void setLabel(MethodDescriptor descriptor, Label label) {
        if (JavaLabel.CONSTRUCTOR.name().equals(label.name())) {
            descriptor.setConstructor(Boolean.TRUE);
        }
    }
}
