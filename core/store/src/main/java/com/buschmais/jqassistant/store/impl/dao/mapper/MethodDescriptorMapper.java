package com.buschmais.jqassistant.store.impl.dao.mapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;

import com.buschmais.jqassistant.store.api.DescriptorDAO.CoreLabel;
import com.buschmais.jqassistant.store.api.model.AbstractDescriptor;
import com.buschmais.jqassistant.store.api.model.ClassDescriptor;
import com.buschmais.jqassistant.store.api.model.MethodDescriptor;
import com.buschmais.jqassistant.store.impl.model.RelationType;

public class MethodDescriptorMapper extends
		AbstractDescriptorMapper<MethodDescriptor> {

	@Override
	public Class<MethodDescriptor> getJavaType() {
		return MethodDescriptor.class;
	}

	@Override
	public CoreLabel getCoreLabel() {
		return CoreLabel.METHOD;
	}

	@Override
	public Index<Node> getIndex() {
		return null;
	}

	@Override
	public MethodDescriptor createInstance() {
		return new MethodDescriptor();
	}

	@Override
	public Map<RelationType, Set<? extends AbstractDescriptor>> getRelations(
			MethodDescriptor descriptor) {
		Map<RelationType, Set<? extends AbstractDescriptor>> relations = new HashMap<RelationType, Set<? extends AbstractDescriptor>>();
		relations.put(RelationType.DEPENDS_ON, descriptor.getDependencies());
		relations.put(RelationType.THROWS, descriptor.getDeclaredThrowables());
		return relations;
	}

	@Override
	protected void setRelation(MethodDescriptor descriptor,
			RelationType relation, AbstractDescriptor target) {
		switch (relation) {
		case DEPENDS_ON:
			descriptor.getDependencies().add((ClassDescriptor) target);
			break;
		case THROWS:
			descriptor.getDeclaredThrowables().add((ClassDescriptor) target);
			break;
		default:
			throw new IllegalArgumentException("Unsupported relation type "
					+ relation);
		}
	}

}
