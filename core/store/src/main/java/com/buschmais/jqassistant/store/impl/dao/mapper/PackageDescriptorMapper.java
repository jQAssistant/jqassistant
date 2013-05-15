package com.buschmais.jqassistant.store.impl.dao.mapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;

import com.buschmais.jqassistant.store.api.model.AbstractDescriptor;
import com.buschmais.jqassistant.store.api.model.PackageDescriptor;
import com.buschmais.jqassistant.store.impl.model.NodeType;
import com.buschmais.jqassistant.store.impl.model.RelationType;

public class PackageDescriptorMapper extends
		AbstractDescriptorMapper<PackageDescriptor> {

	private final Index<Node> packageIndex;

	public PackageDescriptorMapper(Index<Node> classIndex) {
		this.packageIndex = classIndex;
	}

	@Override
	public Class<PackageDescriptor> getJavaType() {
		return PackageDescriptor.class;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.PACKAGE;
	}

	@Override
	public Index<Node> getIndex() {
		return packageIndex;
	}

	@Override
	public PackageDescriptor createInstance() {
		return new PackageDescriptor();
	}

	@Override
	public Map<RelationType, Set<? extends AbstractDescriptor>> getRelations(
			PackageDescriptor descriptor) {
		Map<RelationType, Set<? extends AbstractDescriptor>> relations = new HashMap<RelationType, Set<? extends AbstractDescriptor>>();
		relations.put(RelationType.CONTAINS, descriptor.getContains());
		return relations;
	}

	@Override
	public void setRelation(PackageDescriptor descriptor,
			RelationType relation, AbstractDescriptor target) {
		switch (relation) {
		case CONTAINS:
			descriptor.getContains().add(target);
			break;
		default:
			throw new IllegalArgumentException("Unsupported relation type "
					+ relation);
		}
	}

}
