package com.buschmais.jqassistant.core.store.impl.dao.mapper;

import com.buschmais.jqassistant.core.model.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.JavaType;
import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.VisibilityModifier;
import com.buschmais.jqassistant.core.model.api.descriptor.value.AnnotationValueDescriptor;
import com.buschmais.jqassistant.core.store.api.model.PrimaryLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

import java.util.HashSet;
import java.util.Set;

import static com.buschmais.jqassistant.core.store.impl.dao.mapper.NodeLabel.TYPE;
import static com.buschmais.jqassistant.core.store.impl.dao.mapper.Label.label;

/**
 * A store for {@link TypeDescriptor}s.
 */
public class TypeDescriptorMapper extends AbstractDescriptorMapper<TypeDescriptor, TypeDescriptorMapper.Property, TypeDescriptorMapper.Relation> {

    enum Property {
        SIGNATURE,
        ABSTRACT,
        VISIBILITY,
        STATIC,
        FINAL,
        SYNTHETIC;
    }

    enum Relation implements RelationshipType {
        ANNOTATED_BY,
        CONTAINS,
        DEPENDS_ON,
        IMPLEMENTS,
        EXTENDS;
    }

    @Override
    public Set<Class<? extends TypeDescriptor>> getJavaType() {
        Set<Class<? extends TypeDescriptor>> javaTypes = new HashSet<>();
        javaTypes.add(TypeDescriptor.class);
        return javaTypes;
    }

    @Override
    public PrimaryLabel getPrimaryLabel() {
        return TYPE;
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
    public Class<? extends TypeDescriptor> getType(Set<Label> labels) {
        return TypeDescriptor.class;
    }

    @Override
    public TypeDescriptor createInstance(Class<? extends TypeDescriptor> type) {
        return new TypeDescriptor();
    }

    @Override
    public Set<? extends Descriptor> getRelation(TypeDescriptor descriptor, Relation relation) {
        switch (relation) {
            case ANNOTATED_BY:
                return descriptor.getAnnotatedBy();
            case CONTAINS:
                return descriptor.getContains();
            case DEPENDS_ON:
                return descriptor.getDependencies();
            case IMPLEMENTS:
                return descriptor.getInterfaces();
            case EXTENDS:
                return asSet(descriptor.getSuperClass());
            default:
                break;
        }
        return null;
    }

    @Override
    protected void setRelation(TypeDescriptor descriptor, Relation relation, Set<? extends Descriptor> target) {
        switch (relation) {
            case ANNOTATED_BY:
                descriptor.setAnnotatedBy((Set<AnnotationValueDescriptor>) target);
                break;
            case CONTAINS:
                descriptor.setContains((Set<Descriptor>) target);
                break;
            case DEPENDS_ON:
                descriptor.setDependencies((Set<TypeDescriptor>) target);
                break;
            case IMPLEMENTS:
                descriptor.setInterfaces((Set<TypeDescriptor>) target);
                break;
            case EXTENDS:
                descriptor.setSuperClass((TypeDescriptor) getSingleEntry(target));
                break;
            default:
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getProperty(TypeDescriptor descriptor, Property property) {
        switch (property) {
            case SIGNATURE:
                return descriptor.getSignature();
            case ABSTRACT:
                return descriptor.isAbstract();
            case VISIBILITY:
                return descriptor.getVisibility() != null ? descriptor.getVisibility().name() : null;
            case STATIC:
                return descriptor.isStatic();
            case FINAL:
                return descriptor.isFinal();
            case SYNTHETIC:
                return descriptor.isSynthetic();
            default:
                break;
        }
        return null;
    }

    @Override
    public void setProperty(TypeDescriptor descriptor, Property property, Object value) {
        switch (property) {
            case SIGNATURE:
                descriptor.setSignature((String) value);
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
    public Set<Label> getLabels(TypeDescriptor descriptor) {
        Set<Label> labels = new HashSet<>();
        final JavaType javaType = descriptor.getJavaType();
        if (javaType != null) {
            labels.add(label(javaType));
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