package com.buschmais.jqassistant.store.impl.dao.mapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;

import com.buschmais.jqassistant.store.api.DescriptorDAO.CoreLabel;
import com.buschmais.jqassistant.store.api.model.AbstractDescriptor;
import com.buschmais.jqassistant.store.api.model.ClassDescriptor;
import com.buschmais.jqassistant.store.api.model.FieldDescriptor;
import com.buschmais.jqassistant.store.impl.model.RelationType;

public class FieldDescriptorMapper extends
		AbstractDescriptorMapper<FieldDescriptor> {

	@Override
	public Class<FieldDescriptor> getJavaType() {
		return FieldDescriptor.class;
	}

	@Override
	public CoreLabel getCoreLabel() {
		return CoreLabel.FIELD;
	}

	@Override
	public Index<Node> getIndex() {
		return null;
	}

	@Override
	public FieldDescriptor createInstance() {
		return new FieldDescriptor();
	}

	@Override
	public Map<RelationType, Set<? extends AbstractDescriptor>> getRelations(
			FieldDescriptor descriptor) {
		Map<RelationType, Set<? extends AbstractDescriptor>> relations = new HashMap<RelationType, Set<? extends AbstractDescriptor>>();
		relations.put(RelationType.DEPENDS_ON, descriptor.getDependencies());
		return relations;
	}

	@Override
	protected void setRelation(FieldDescriptor descriptor,
			RelationType relation, AbstractDescriptor target) {
		switch (relation) {
		case DEPENDS_ON:
			descriptor.getDependencies().add((ClassDescriptor) target);
			break;
		default:
			throw new IllegalArgumentException("Unsupported relation type "
					+ relation);
		}
	}

}
