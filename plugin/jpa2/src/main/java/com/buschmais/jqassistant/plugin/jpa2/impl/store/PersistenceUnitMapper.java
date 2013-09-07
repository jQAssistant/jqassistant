package com.buschmais.jqassistant.plugin.jpa2.impl.store;

import com.buschmais.jqassistant.core.model.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.store.api.model.PrimaryLabel;
import com.buschmais.jqassistant.core.store.impl.dao.mapper.AbstractDescriptorMapper;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.buschmais.jqassistant.plugin.jpa2.impl.store.Jpa2Label.PERSISTENCEUNIT;

/**
 * {@link AbstractDescriptorMapper} for {@PersistenceUnitDescriptor}s.
 */
public class PersistenceUnitMapper extends AbstractDescriptorMapper<PersistenceUnitDescriptor, PersistenceUnitMapper.PersistenceUnitProperty, PersistenceUnitMapper.PersistenceUnitRelation> {

    enum PersistenceUnitProperty {
    }

    enum PersistenceUnitRelation implements RelationshipType {
        CONTAINS;
    }


    @Override
    public Set<Class<? extends PersistenceUnitDescriptor>> getJavaType() {
        Set<Class<? extends PersistenceUnitDescriptor>> javaTypes = new HashSet<>();
        javaTypes.add(PersistenceUnitDescriptor.class);
        return javaTypes;
    }

    @Override
    protected Class<PersistenceUnitProperty> getPropertyKeys() {
        return PersistenceUnitProperty.class;
    }

    @Override
    protected Class<PersistenceUnitRelation> getRelationKeys() {
        return PersistenceUnitRelation.class;
    }

    @Override
    public PersistenceUnitDescriptor createInstance(Class<? extends PersistenceUnitDescriptor> type) {
        return new PersistenceUnitDescriptor();
    }

    @Override
    public Class<? extends PersistenceUnitDescriptor> getType(Set<Label> labels) {
        return PersistenceUnitDescriptor.class;
    }

    @Override
    public PrimaryLabel getPrimaryLabel() {
        return PERSISTENCEUNIT;
    }

    @Override
    public void setProperty(PersistenceUnitDescriptor descriptor, PersistenceUnitProperty property, Object value) {
    }

    @Override
    protected Object getProperty(PersistenceUnitDescriptor descriptor, PersistenceUnitProperty property) {
        return null;
    }

    @Override
    protected void setRelation(PersistenceUnitDescriptor descriptor, PersistenceUnitRelation relation, Set<? extends Descriptor> target) {
        switch (relation) {
            case CONTAINS:
                descriptor.setContains((Set<TypeDescriptor>) target);
                break;
            default:
                break;
        }
    }

    @Override
    public Set<? extends Descriptor> getRelation(PersistenceUnitDescriptor descriptor, PersistenceUnitRelation relation) {
        switch (relation) {
            case CONTAINS:
                return descriptor.getContains();
            default:
                break;
        }
        return null;
    }
}
