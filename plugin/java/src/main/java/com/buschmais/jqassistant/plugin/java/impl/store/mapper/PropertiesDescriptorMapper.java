package com.buschmais.jqassistant.plugin.java.impl.store.mapper;

import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.store.api.model.IndexedLabel;
import com.buschmais.jqassistant.core.store.impl.dao.mapper.AbstractDescriptorMapper;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PrimitiveValueDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PropertiesDescriptor;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.emptyList;

/**
 * A mapper for properties.
 */
public class PropertiesDescriptorMapper extends AbstractDescriptorMapper<PropertiesDescriptor, PropertiesDescriptorMapper.Property, PropertiesDescriptorMapper.Relation> {

    enum Property {
    }

    enum Relation implements RelationshipType {
        HAS;
    }

    @Override
    public Set<Class<? extends PropertiesDescriptor>> getJavaType() {
        Set<Class<? extends PropertiesDescriptor>> javaTypes = new HashSet<>();
        javaTypes.add(PropertiesDescriptor.class);
        return javaTypes;
    }

    @Override
    public IndexedLabel getPrimaryLabel() {
        return JavaLabel.PROPERTIES;
    }

    @Override
    public PropertiesDescriptor createInstance(Class<? extends PropertiesDescriptor> type) {
        return new PropertiesDescriptor();
    }

    @Override
    public Class<? extends PropertiesDescriptor> getType(Set<Label> labels) {
        return PropertiesDescriptor.class;
    }

    @Override
    protected Class<Relation> getRelationKeys() {
        return Relation.class;
    }

    @Override
    protected Class<Property> getPropertyKeys() {
        return Property.class;
    }

    @Override
    protected Set<? extends Descriptor> getRelation(PropertiesDescriptor descriptor, Relation relation) {
        switch (relation) {
            case HAS:
                return descriptor.getProperties();
            default:
                break;
        }
        return null;
    }

    @Override
    protected void setRelation(PropertiesDescriptor descriptor, Relation relation, Set<? extends Descriptor> targets) {
        switch (relation) {
            case HAS:
                descriptor.setProperties((Set<PrimitiveValueDescriptor>) targets);
            default:
                break;
        }
    }

    @Override
    protected Object getProperty(PropertiesDescriptor descriptor, Property property) {
        return null;
    }

    @Override
    protected void setProperty(PropertiesDescriptor descriptor, Property property, Object value) {
    }

    @Override
    public Set<? extends Label> getLabels(PropertiesDescriptor descriptor) {
        return Collections.emptySet();
    }

    @Override
    public void setLabel(PropertiesDescriptor descriptor, Label label) {
    }
}
