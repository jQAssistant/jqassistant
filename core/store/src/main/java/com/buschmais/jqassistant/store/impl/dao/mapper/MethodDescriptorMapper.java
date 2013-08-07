package com.buschmais.jqassistant.store.impl.dao.mapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.core.model.api.descriptor.AbstractDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.MethodDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.VisibilityModifier;
import com.buschmais.jqassistant.store.api.model.NodeLabel;
import com.buschmais.jqassistant.store.api.model.NodeProperty;
import com.buschmais.jqassistant.store.api.model.Relation;

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
                descriptor.getAnnotatedBy().add((TypeDescriptor) target);
                break;
            case DEPENDS_ON:
                descriptor.getDependencies().add((TypeDescriptor) target);
                break;
            case THROWS:
                descriptor.getDeclaredThrowables().add((TypeDescriptor) target);
                break;
            default:
        }
    }


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<NodeProperty, Object> getProperties(MethodDescriptor descriptor) {
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
		if (descriptor.isNative() != null) {
			properties.put(NodeProperty.NATIVE, descriptor.isNative());
		}
		return properties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setProperty(MethodDescriptor descriptor, NodeProperty property, Object value) {
		if (value != null) {
			super.setProperty(descriptor, property, value);
			switch (property) {
			case NATIVE:
				descriptor.setNative((Boolean) value);
				break;
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
