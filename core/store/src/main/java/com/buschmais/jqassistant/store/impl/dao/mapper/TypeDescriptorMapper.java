package com.buschmais.jqassistant.store.impl.dao.mapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.core.model.api.descriptor.AbstractDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.VisibilityModifier;
import com.buschmais.jqassistant.store.api.model.NodeLabel;
import com.buschmais.jqassistant.store.api.model.NodeProperty;
import com.buschmais.jqassistant.store.api.model.Relation;

public class TypeDescriptorMapper extends AbstractDescriptorMapper<TypeDescriptor> {

    @Override
    public Class<TypeDescriptor> getJavaType() {
        return TypeDescriptor.class;
    }

    @Override
    public NodeLabel getCoreLabel() {
        return NodeLabel.TYPE;
    }

    @Override
    public TypeDescriptor createInstance() {
        return new TypeDescriptor();
    }

    @Override
    public Map<Relation, Set<? extends AbstractDescriptor>> getRelations(TypeDescriptor descriptor) {
        Map<Relation, Set<? extends AbstractDescriptor>> relations = new HashMap<Relation, Set<? extends AbstractDescriptor>>();
        relations.put(Relation.ANNOTATED_BY, descriptor.getAnnotatedBy());
        relations.put(Relation.CONTAINS, descriptor.getContains());
        relations.put(Relation.DEPENDS_ON, descriptor.getDependencies());
        relations.put(Relation.IMPLEMENTS, descriptor.getInterfaces());
        relations.put(Relation.EXTENDS, asSet(descriptor.getSuperClass()));
        return relations;
    }

    @Override
    protected void setRelation(TypeDescriptor descriptor, Relation relation, AbstractDescriptor target) {
        switch (relation) {
            case ANNOTATED_BY:
                descriptor.getAnnotatedBy().add((TypeDescriptor) target);
                break;
            case CONTAINS:
                descriptor.getContains().add(target);
                break;
            case DEPENDS_ON:
                descriptor.getDependencies().add((TypeDescriptor) target);
                break;
            case IMPLEMENTS:
                descriptor.getInterfaces().add((TypeDescriptor) target);
                break;
            case EXTENDS:
                descriptor.setSuperClass((TypeDescriptor) target);
                break;
            default:
        }
    }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<NodeProperty, Object> getProperties(TypeDescriptor descriptor) {
		Map<NodeProperty, Object> properties = super.getProperties(descriptor);
		properties.put(NodeProperty.ABSTRACT, descriptor.isAbstract());
		if (descriptor.getVisibility() != null) {
			properties.put(NodeProperty.VISIBILITY, descriptor.getVisibility().name());
		}
		if (descriptor.isStatic() != null) {
			properties.put(NodeProperty.STATIC, descriptor.isStatic());
		}
		if (descriptor.isFinal() != null) {
			properties.put(NodeProperty.FINAL, descriptor.isFinal());
		}
		return properties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setProperty(TypeDescriptor descriptor, NodeProperty property, Object value) {
		if (value != null) {
			super.setProperty(descriptor, property, value);
			switch (property) {
			case ABSTRACT:
				descriptor.setAbstract((Boolean) value);
				break;
			case STATIC:
				descriptor.setStatic((Boolean) value);
				break;
			case FINAL:
				descriptor.setFinal((Boolean) value);
				break;
			case VISIBILITY:
				descriptor.setVisibility(VisibilityModifier.valueOf((String) value));
				break;
			default:
				break;
			}
		}
	}

}
