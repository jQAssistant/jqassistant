package com.buschmais.jqassistant.store.impl.dao.mapper;

import com.buschmais.jqassistant.core.model.api.descriptor.AbstractDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.PackageDescriptor;
import com.buschmais.jqassistant.store.api.model.NodeLabel;
import com.buschmais.jqassistant.store.api.model.Relation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A mapper for {@link PackageDescriptor}s.
 */
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
