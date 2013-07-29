package com.buschmais.jqassistant.store.impl.dao.mapper;

import com.buschmais.jqassistant.core.model.api.descriptor.AbstractDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.ClassDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.FieldDescriptor;
import com.buschmais.jqassistant.store.api.model.NodeLabel;
import com.buschmais.jqassistant.store.api.model.Relation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FieldDescriptorMapper extends AbstractDescriptorMapper<FieldDescriptor> {

    @Override
    public Class<FieldDescriptor> getJavaType() {
        return FieldDescriptor.class;
    }

    @Override
    public NodeLabel getCoreLabel() {
        return NodeLabel.FIELD;
    }

    @Override
    public FieldDescriptor createInstance() {
        return new FieldDescriptor();
    }

    @Override
    public Map<Relation, Set<? extends AbstractDescriptor>> getRelations(FieldDescriptor descriptor) {
        Map<Relation, Set<? extends AbstractDescriptor>> relations = new HashMap<Relation, Set<? extends AbstractDescriptor>>();
        relations.put(Relation.ANNOTATED_BY, descriptor.getAnnotatedBy());
        relations.put(Relation.DEPENDS_ON, descriptor.getDependencies());
        return relations;
    }

    @Override
    protected void setRelation(FieldDescriptor descriptor, Relation relation, AbstractDescriptor target) {
        switch (relation) {
            case ANNOTATED_BY:
                descriptor.getAnnotatedBy().add((ClassDescriptor) target);
                break;
            case DEPENDS_ON:
                descriptor.getDependencies().add((ClassDescriptor) target);
                break;
            default:
                throw new IllegalArgumentException("Unsupported relation type " + relation);
        }
    }

}
