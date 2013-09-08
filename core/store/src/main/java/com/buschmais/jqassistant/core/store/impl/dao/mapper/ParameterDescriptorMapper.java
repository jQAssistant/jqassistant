package com.buschmais.jqassistant.core.store.impl.dao.mapper;

import com.buschmais.jqassistant.core.model.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.ParameterDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.value.AnnotationValueDescriptor;
import com.buschmais.jqassistant.core.store.api.model.IndexedLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

import java.util.HashSet;
import java.util.Set;

import static com.buschmais.jqassistant.core.store.impl.dao.mapper.JavaLabel.PARAMETER;

/**
 * A store for {@link ParameterDescriptor}s.
 */
public class ParameterDescriptorMapper extends AbstractDescriptorMapper<ParameterDescriptor, ParameterDescriptorMapper.Property, ParameterDescriptorMapper.Relation> {

    enum Property {

    }

    enum Relation implements RelationshipType{
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
        return PARAMETER;
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
            case ANNOTATED_BY:return descriptor.getAnnotatedBy();
            case DEPENDS_ON:return descriptor.getDependencies();
            default:break;
        }
        return null;
    }

    @Override
    protected void setRelation(ParameterDescriptor descriptor, Relation relation, Set<?extends Descriptor> target) {
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
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProperty(ParameterDescriptor descriptor, Property property, Object value) {
    }
}
