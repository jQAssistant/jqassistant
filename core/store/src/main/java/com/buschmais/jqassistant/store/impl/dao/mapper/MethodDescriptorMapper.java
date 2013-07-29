package com.buschmais.jqassistant.store.impl.dao.mapper;

import com.buschmais.jqassistant.core.model.api.descriptor.AbstractDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.ClassDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.MethodDescriptor;
import com.buschmais.jqassistant.store.api.model.NodeLabel;
import com.buschmais.jqassistant.store.api.model.Relation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MethodDescriptorMapper extends AbstractDescriptorMapper<MethodDescriptor> {

    @Override
    public Class<MethodDescriptor> getJavaType() {
        return MethodDescriptor.class;
    }

    @Override
    public NodeLabel getCoreLabel() {
        return NodeLabel.METHOD;
    }

    @Override
    public MethodDescriptor createInstance() {
        return new MethodDescriptor();
    }

    @Override
    public Map<Relation, Set<? extends AbstractDescriptor>> getRelations(MethodDescriptor descriptor) {
        Map<Relation, Set<? extends AbstractDescriptor>> relations = new HashMap<Relation, Set<? extends AbstractDescriptor>>();
        relations.put(Relation.ANNOTATED_BY, descriptor.getAnnotatedBy());
        relations.put(Relation.DEPENDS_ON, descriptor.getDependencies());
        relations.put(Relation.THROWS, descriptor.getDeclaredThrowables());
        return relations;
    }

    @Override
    protected void setRelation(MethodDescriptor descriptor, Relation relation, AbstractDescriptor target) {
        switch (relation) {
            case ANNOTATED_BY:
                descriptor.getAnnotatedBy().add((ClassDescriptor) target);
                break;
            case DEPENDS_ON:
                descriptor.getDependencies().add((ClassDescriptor) target);
                break;
            case THROWS:
                descriptor.getDeclaredThrowables().add((ClassDescriptor) target);
                break;
            default:
        }
    }

}
