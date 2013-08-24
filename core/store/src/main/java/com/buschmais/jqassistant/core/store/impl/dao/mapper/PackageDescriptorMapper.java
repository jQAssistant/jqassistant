package com.buschmais.jqassistant.core.store.impl.dao.mapper;

import com.buschmais.jqassistant.core.model.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.PackageDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.ValueDescriptor;
import com.buschmais.jqassistant.core.store.api.model.NodeLabel;
import com.buschmais.jqassistant.core.store.api.model.Relation;
import org.neo4j.graphdb.Label;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.buschmais.jqassistant.core.store.api.model.NodeLabel.PACKAGE;

/**
 * A mapper for {@link PackageDescriptor}s.
 */
public class PackageDescriptorMapper extends AbstractDescriptorMapper<PackageDescriptor> {

    @Override
    public Set<Class<? extends PackageDescriptor>> getJavaType() {
        Set<Class<? extends PackageDescriptor>> javaTypes = new HashSet<>();
        javaTypes.add(PackageDescriptor.class);
        return javaTypes;
    }

    @Override
    public NodeLabel getCoreLabel() {
        return  PACKAGE;
    }

    @Override
    public Class<? extends PackageDescriptor> getType(Set<Label> labels) {
        return PackageDescriptor.class;
    }

    @Override
    public PackageDescriptor createInstance(Class<? extends PackageDescriptor> type) {
        return new PackageDescriptor();
    }

    @Override
    public Map<Relation, Set<? extends Descriptor>> getRelations(PackageDescriptor descriptor) {
        Map<Relation, Set<? extends Descriptor>> relations = new HashMap<Relation, Set<? extends Descriptor>>();
        relations.put(Relation.CONTAINS, descriptor.getContains());
        return relations;
    }

    @Override
    protected void setRelation(PackageDescriptor descriptor, Relation relation, Descriptor target) {
        switch (relation) {
            case CONTAINS:
                descriptor.getContains().add(target);
                break;
            default:
        }
    }

}
