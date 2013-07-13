package com.buschmais.jqassistant.store.impl.dao.mapper;

import com.buschmais.jqassistant.store.api.DescriptorDAO.CoreLabel;
import com.buschmais.jqassistant.store.api.model.AbstractDescriptor;
import com.buschmais.jqassistant.store.api.model.ClassDescriptor;
import com.buschmais.jqassistant.store.api.model.MethodDescriptor;
import com.buschmais.jqassistant.store.impl.model.RelationType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MethodDescriptorMapper extends AbstractDescriptorMapper<MethodDescriptor> {

    @Override
    public Class<MethodDescriptor> getJavaType() {
        return MethodDescriptor.class;
    }

    @Override
    public CoreLabel getCoreLabel() {
        return CoreLabel.METHOD;
    }

    @Override
    public MethodDescriptor createInstance() {
        return new MethodDescriptor();
    }

    @Override
    public Map<RelationType, Set<? extends AbstractDescriptor>> getRelations(MethodDescriptor descriptor) {
        Map<RelationType, Set<? extends AbstractDescriptor>> relations = new HashMap<RelationType, Set<? extends AbstractDescriptor>>();
        relations.put(RelationType.DEPENDS_ON, descriptor.getDependencies());
        relations.put(RelationType.THROWS, descriptor.getDeclaredThrowables());
        return relations;
    }

    @Override
    protected void setRelation(MethodDescriptor descriptor, RelationType relation, AbstractDescriptor target) {
        switch (relation) {
            case DEPENDS_ON:
                descriptor.getDependencies().add((ClassDescriptor) target);
                break;
            case THROWS:
                descriptor.getDeclaredThrowables().add((ClassDescriptor) target);
                break;
            default:
                throw new IllegalArgumentException("Unsupported relation type " + relation);
        }
    }

}
