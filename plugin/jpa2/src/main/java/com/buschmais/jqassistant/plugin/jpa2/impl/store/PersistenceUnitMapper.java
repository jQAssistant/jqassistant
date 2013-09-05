package com.buschmais.jqassistant.plugin.jpa2.impl.store;

import com.buschmais.jqassistant.core.model.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.store.api.model.PrimaryLabel;
import com.buschmais.jqassistant.core.store.api.model.Relation;
import com.buschmais.jqassistant.core.store.impl.dao.mapper.AbstractDescriptorMapper;
import org.neo4j.graphdb.Label;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.buschmais.jqassistant.plugin.jpa2.impl.store.JpaLabel.PERSISTENCEUNIT;

/**
 * {@link AbstractDescriptorMapper} for {@PersistenceUnitDescriptor}s.
 */
public class PersistenceUnitMapper extends AbstractDescriptorMapper<PersistenceUnitDescriptor> {

    @Override
    public Set<Class<? extends PersistenceUnitDescriptor>> getJavaType() {
        Set<Class<? extends PersistenceUnitDescriptor>> javaTypes = new HashSet<>();
        javaTypes.add(PersistenceUnitDescriptor.class);
        return javaTypes;
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
    protected void setRelation(PersistenceUnitDescriptor descriptor, Relation relation, Descriptor target) {
    }


    @Override
    public PrimaryLabel getPrimaryLabel() {
        return PERSISTENCEUNIT;
    }

    @Override
    public Map<Relation, Set<? extends Descriptor>> getRelations(PersistenceUnitDescriptor descriptor) {
        return null;
    }
}
