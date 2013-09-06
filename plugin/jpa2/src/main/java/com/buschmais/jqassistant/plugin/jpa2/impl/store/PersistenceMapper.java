package com.buschmais.jqassistant.plugin.jpa2.impl.store;

import com.buschmais.jqassistant.core.model.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.store.api.model.PrimaryLabel;
import com.buschmais.jqassistant.core.store.api.model.Relation;
import com.buschmais.jqassistant.core.store.impl.dao.mapper.AbstractDescriptorMapper;
import org.neo4j.graphdb.Label;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.buschmais.jqassistant.plugin.jpa2.impl.store.Jpa2Label.PERSISTENCE;
import static com.buschmais.jqassistant.plugin.jpa2.impl.store.Jpa2Label.PERSISTENCEUNIT;

/**
 * {@link AbstractDescriptorMapper} for {@PersistenceUnitDescriptor}s.
 */
public class PersistenceMapper extends AbstractDescriptorMapper<PersistenceDescriptor> {

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
    public PrimaryLabel getPrimaryLabel() {
        return PERSISTENCE;
    }

    @Override
    protected void setRelation(PersistenceDescriptor descriptor, Relation relation, Descriptor target) {
        switch (relation) {
            case CONTAINS:
                descriptor.getContains().add((PersistenceUnitDescriptor) target);
                break;
            default:
                break;
        }
    }

    @Override
    public Map<Relation, Set<? extends Descriptor>> getRelations(PersistenceDescriptor descriptor) {
        Map<Relation, Set<? extends Descriptor>> relations = new HashMap<>();
        relations.put(Relation.CONTAINS, descriptor.getContains());
        return relations;
    }
}
