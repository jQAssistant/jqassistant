/**
 *
 */
package com.buschmais.jqassistant.store.impl.dao.mapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.core.model.api.descriptor.AbstractDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.ArtifactDescriptor;
import com.buschmais.jqassistant.store.api.model.NodeLabel;
import com.buschmais.jqassistant.store.api.model.NodeProperty;
import com.buschmais.jqassistant.store.api.model.Relation;

/**
 * Maps the {@link ArtifactDescriptor} to nodes and relationships.
 *
 * @author Herklotz
 */
public class ArtifactDescriptorMapper extends AbstractDescriptorMapper<ArtifactDescriptor> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<ArtifactDescriptor> getJavaType() {
		return ArtifactDescriptor.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeLabel getCoreLabel() {
		return NodeLabel.ARTIFACT;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArtifactDescriptor createInstance() {
		return new ArtifactDescriptor();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<NodeProperty, Object> getProperties(ArtifactDescriptor descriptor) {
		Map<NodeProperty, Object> properties = super.getProperties(descriptor);
		properties.put(NodeProperty.GROUP, descriptor.getGroup());
		properties.put(NodeProperty.NAME, descriptor.getName());
		properties.put(NodeProperty.VERSION, descriptor.getVersion());
		return properties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setProperty(ArtifactDescriptor descriptor, NodeProperty property, Object value) {
		super.setProperty(descriptor, property, value);

		switch (property) {
		case GROUP:
			descriptor.setGroup((String) value);
			break;
		case NAME:
			descriptor.setName((String) value);
			break;
		case VERSION:
			descriptor.setVersion((String) value);
			break;
		default:
			break;
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<Relation, Set<? extends AbstractDescriptor>> getRelations(ArtifactDescriptor descriptor) {
		Map<Relation, Set<? extends AbstractDescriptor>> relations = new HashMap<Relation, Set<? extends AbstractDescriptor>>();
		relations.put(Relation.CONTAINS, descriptor.getContains());
		return relations;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setRelation(ArtifactDescriptor descriptor, Relation relation, AbstractDescriptor target) {
		switch (relation) {
		case CONTAINS:
			descriptor.getContains().add(target);
			break;
		default:
		}
	}

}
