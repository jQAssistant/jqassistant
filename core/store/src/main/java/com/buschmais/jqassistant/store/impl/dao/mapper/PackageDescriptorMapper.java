package com.buschmais.jqassistant.store.impl.dao.mapper;

import com.buschmais.jqassistant.store.api.model.graph.NodeLabel;
import com.buschmais.jqassistant.store.api.model.descriptor.AbstractDescriptor;
import com.buschmais.jqassistant.store.api.model.descriptor.PackageDescriptor;
import com.buschmais.jqassistant.store.api.model.graph.Relation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PackageDescriptorMapper extends AbstractDescriptorMapper<PackageDescriptor> {

    @Override
    public Class<PackageDescriptor> getJavaType() {
        return PackageDescriptor.class;
    }

    @Override
    public NodeLabel getCoreLabel() {
        return NodeLabel.PACKAGE;
    }

    @Override
    public PackageDescriptor createInstance() {
        return new PackageDescriptor();
    }

    @Override
    public Map<Relation, Set<? extends AbstractDescriptor>> getRelations(PackageDescriptor descriptor) {
        Map<Relation, Set<? extends AbstractDescriptor>> relations = new HashMap<Relation, Set<? extends AbstractDescriptor>>();
        relations.put(Relation.CONTAINS, descriptor.getContains());
        return relations;
    }

    @Override
    protected void setRelation(PackageDescriptor descriptor, Relation relation, AbstractDescriptor target) {
        switch (relation) {
            case CONTAINS:
                descriptor.getContains().add(target);
                break;
            default:
                throw new IllegalArgumentException("Unsupported relation type " + relation);
        }
    }

}
