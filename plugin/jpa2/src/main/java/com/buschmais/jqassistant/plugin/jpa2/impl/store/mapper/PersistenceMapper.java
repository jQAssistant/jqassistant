package com.buschmais.jqassistant.plugin.jpa2.impl.store.mapper;

import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.store.api.model.IndexedLabel;
import com.buschmais.jqassistant.core.store.impl.dao.mapper.AbstractDescriptorMapper;
import com.buschmais.jqassistant.plugin.jpa2.impl.store.descriptor.PersistenceDescriptor;
import com.buschmais.jqassistant.plugin.jpa2.impl.store.descriptor.PersistenceUnitDescriptor;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

import java.util.HashSet;
import java.util.Set;

import static com.buschmais.jqassistant.plugin.jpa2.impl.store.mapper.Jpa2Label.JPA;
import static com.buschmais.jqassistant.plugin.jpa2.impl.store.mapper.Jpa2Label.PERSISTENCE;

/**
 * {@link AbstractDescriptorMapper} for {@PersistenceUnitDescriptor}s.
 */
public class PersistenceMapper extends AbstractDescriptorMapper<PersistenceDescriptor, PersistenceMapper.Property, PersistenceMapper.Relation> {

    public enum Property {
        VERSION;
    }

    public enum Relation implements RelationshipType {
        CONTAINS;
    }

    @Override
    public Set<Class<? extends PersistenceDescriptor>> getJavaType() {
        Set<Class<? extends PersistenceDescriptor>> javaTypes = new HashSet<>();
        javaTypes.add(PersistenceDescriptor.class);
        return javaTypes;
    }

    @Override
    public PersistenceDescriptor createInstance(Class<? extends PersistenceDescriptor> type) {
        return new PersistenceDescriptor();
    }

    @Override
    public Class<? extends PersistenceDescriptor> getType(Set<Label> labels) {
        return PersistenceDescriptor.class;
    }

    @Override
    public IndexedLabel getPrimaryLabel() {
        return PERSISTENCE;
    }

    @Override
    public Class<Property> getPropertyKeys() {
        return Property.class;
    }

    @Override
    public Class<Relation> getRelationKeys() {
        return Relation.class;
    }

    @Override
    public Object getProperty(PersistenceDescriptor descriptor, Property property) {
        switch (property) {
            case VERSION:
                return descriptor.getVersion();
            default:
                break;
        }
        return null;
    }

    @Override
    public void setProperty(PersistenceDescriptor descriptor, Property property, Object value) {
        switch (property) {
            case VERSION:
                descriptor.setVersion((String) value);
                break;
            default:
                break;
        }
    }

    @Override
    public void setRelation(PersistenceDescriptor descriptor, Relation relation, Set<? extends Descriptor> targets) {
        switch (relation) {
            case CONTAINS:
                descriptor.setContains((Set<PersistenceUnitDescriptor>) targets);
                break;
            default:
                break;
        }
    }

    @Override
    public Set<? extends Descriptor> getRelation(PersistenceDescriptor descriptor, Relation relation) {
        switch (relation) {
            case CONTAINS:
                return descriptor.getContains();
            default:
                break;
        }
        return null;
    }

    @Override
    public Set<? extends Label> getLabels(PersistenceDescriptor descriptor) {
        return asSet(JPA);
    }
}
