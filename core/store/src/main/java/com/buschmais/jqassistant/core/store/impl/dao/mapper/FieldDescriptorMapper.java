package com.buschmais.jqassistant.core.store.impl.dao.mapper;

import com.buschmais.jqassistant.core.model.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.FieldDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.VisibilityModifier;
import com.buschmais.jqassistant.core.model.api.descriptor.value.AnnotationValueDescriptor;
import com.buschmais.jqassistant.core.store.api.model.IndexedLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

import java.util.HashSet;
import java.util.Set;

import static com.buschmais.jqassistant.core.store.impl.dao.mapper.JavaLabel.FIELD;

/**
 * A store for {@link FieldDescriptor}s.
 */
public class FieldDescriptorMapper extends AbstractDescriptorMapper<FieldDescriptor, FieldDescriptorMapper.Property, FieldDescriptorMapper.Relation> {

    enum Property {
        SIGNATURE,
        NAME,
        VISIBILITY,
        STATIC,
        FINAL,
        VOLATILE,
        TRANSIENT,
        SYNTHETIC;
    }

    enum Relation implements RelationshipType {
        ANNOTATED_BY,
        DEPENDS_ON;
    }

    @Override
    public Set<Class<? extends FieldDescriptor>> getJavaType() {
        Set<Class<? extends FieldDescriptor>> javaTypes = new HashSet<>();
        javaTypes.add(FieldDescriptor.class);
        return javaTypes;
    }

    @Override
    public IndexedLabel getPrimaryLabel() {
        return FIELD;
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
    public Class<? extends FieldDescriptor> getType(Set<Label> labels) {
        return FieldDescriptor.class;
    }

    @Override
    public FieldDescriptor createInstance(Class<? extends FieldDescriptor> type) {
        return new FieldDescriptor();
    }

    @Override
    public Set<? extends Descriptor> getRelation(FieldDescriptor descriptor, Relation relation) {
        switch (relation) {
            case ANNOTATED_BY:
                return descriptor.getAnnotatedBy();
            case DEPENDS_ON:
                return descriptor.getDependencies();
            default:
                break;
        }
        return null;
    }

    @Override
    protected void setRelation(FieldDescriptor descriptor, Relation relation, Set<? extends Descriptor> target) {
        switch (relation) {
            case ANNOTATED_BY:
                descriptor.setAnnotatedBy((Set<AnnotationValueDescriptor>) target);
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
    public Object getProperty(FieldDescriptor descriptor, Property property) {
        switch (property) {
            case SIGNATURE:
                return descriptor.getSignature();
            case NAME:
                return descriptor.getName();
            case VISIBILITY:
                return descriptor.getVisibility() != null ? descriptor.getVisibility().name() : null;
            case STATIC:
                return descriptor.isStatic();
            case FINAL:
                return descriptor.isFinal();
            case VOLATILE:
                return descriptor.isVolatile();
            case TRANSIENT:
                return descriptor.isTransient();
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
    public void setProperty(FieldDescriptor descriptor, Property property, Object value) {
        switch (property) {
            case SIGNATURE:
                descriptor.setSignature((String) value);
                break;
            case NAME:
                descriptor.setName((String) value);
                break;
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
