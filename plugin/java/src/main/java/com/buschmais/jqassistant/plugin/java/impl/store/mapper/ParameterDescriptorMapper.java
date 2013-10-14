package com.buschmais.jqassistant.plugin.java.impl.store.mapper;

import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.store.api.model.IndexedLabel;
import com.buschmais.jqassistant.core.store.impl.dao.mapper.AbstractDescriptorMapper;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.AnnotationValueDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.ParameterDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.TypeDescriptor;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

import java.util.HashSet;
import java.util.Set;

/**
 * A store for {@link ParameterDescriptor}s.
 */
public class ParameterDescriptorMapper extends AbstractDescriptorMapper<ParameterDescriptor, ParameterDescriptorMapper.Property, ParameterDescriptorMapper.Relation> {

    enum Property {
        FQN;
    }

    enum Relation implements RelationshipType {
        ANNOTATED_BY,
        DEPENDS_ON;
    }

    @Override
    public Set<Class<? extends ParameterDescriptor>> getJavaType() {
        Set<Class<? extends ParameterDescriptor>> javaTypes = new HashSet<>();
        javaTypes.add(ParameterDescriptor.class);
        return javaTypes;
    }

    @Override
    public IndexedLabel getPrimaryLabel() {
        return JavaLabel.PARAMETER;
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
    public Class<? extends ParameterDescriptor> getType(Set<Label> labels) {
        return ParameterDescriptor.class;
    }

    @Override
    public ParameterDescriptor createInstance(Class<? extends ParameterDescriptor> type) {
        return new ParameterDescriptor();
    }

    @Override
    public Set<? extends Descriptor> getRelation(ParameterDescriptor descriptor, Relation relation) {
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
    protected void setRelation(ParameterDescriptor descriptor, Relation relation, Set<? extends Descriptor> target) {
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
    public Object getProperty(ParameterDescriptor descriptor, Property property) {
        switch (property) {
            case FQN:
                return descriptor.getFullQualifiedName();
            default:
                return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProperty(ParameterDescriptor descriptor, Property property, Object value) {
        switch (property) {
            case FQN:
                descriptor.setFullQualifiedName((String) value);
                break;
            default:
                break;
        }
    }
}
