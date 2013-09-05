package com.buschmais.jqassistant.core.store.impl.dao.mapper;

import com.buschmais.jqassistant.core.model.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.ParameterDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.value.AnnotationValueDescriptor;
import com.buschmais.jqassistant.core.store.api.model.NodeProperty;
import com.buschmais.jqassistant.core.store.api.model.PrimaryLabel;
import com.buschmais.jqassistant.core.store.api.model.Relation;
import org.neo4j.graphdb.Label;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.buschmais.jqassistant.core.store.api.model.NodeLabel.PARAMETER;

/**
 * A store for {@link ParameterDescriptor}s.
 */
public class ParameterDescriptorMapper extends AbstractDescriptorMapper<ParameterDescriptor> {

    @Override
    public Set<Class<? extends ParameterDescriptor>> getJavaType() {
        Set<Class<? extends ParameterDescriptor>> javaTypes = new HashSet<>();
        javaTypes.add(ParameterDescriptor.class);
        return javaTypes;
    }

    @Override
    public PrimaryLabel getPrimaryLabel() {
        return PARAMETER;
    }

    @Override
    public Class<? extends ParameterDescriptor> getType(Set<Label> labels) {
        return ParameterDescriptor.class;
    }

    @Override
    public ParameterDescriptor createInstance(Class<? extends ParameterDescriptor> type) {
        return new ParameterDescriptor();
    }

    @Override
    public Map<Relation, Set<? extends Descriptor>> getRelations(ParameterDescriptor descriptor) {
        Map<Relation, Set<? extends Descriptor>> relations = new HashMap<>();
        relations.put(Relation.ANNOTATED_BY, descriptor.getAnnotatedBy());
        relations.put(Relation.DEPENDS_ON, descriptor.getDependencies());
        return relations;
    }

    @Override
    protected void setRelation(ParameterDescriptor descriptor, Relation relation, Descriptor target) {
        switch (relation) {
            case ANNOTATED_BY:
                descriptor.getAnnotatedBy().add((AnnotationValueDescriptor) target);
                break;
            case DEPENDS_ON:
                descriptor.getDependencies().add((TypeDescriptor) target);
                break;
            default:
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<NodeProperty, Object> getProperties(ParameterDescriptor descriptor) {
        return super.getProperties(descriptor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProperty(ParameterDescriptor descriptor, NodeProperty property, Object value) {
        super.setProperty(descriptor, property, value);
    }
}
