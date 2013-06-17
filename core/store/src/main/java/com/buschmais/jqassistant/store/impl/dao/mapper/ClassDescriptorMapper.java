package com.buschmais.jqassistant.store.impl.dao.mapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;

import com.buschmais.jqassistant.store.api.DescriptorDAO.CoreLabel;
import com.buschmais.jqassistant.store.api.model.AbstractDescriptor;
import com.buschmais.jqassistant.store.api.model.ClassDescriptor;
import com.buschmais.jqassistant.store.impl.model.RelationType;

public class ClassDescriptorMapper extends
		AbstractDescriptorMapper<ClassDescriptor> {

	private final Index<Node> classIndex;

	public ClassDescriptorMapper(Index<Node> classIndex) {
		this.classIndex = classIndex;
	}

	@Override
	public Class<ClassDescriptor> getJavaType() {
		return ClassDescriptor.class;
	}

	@Override
	public CoreLabel getCoreLabel() {
		return CoreLabel.CLASS;
	}

	@Override
	public Index<Node> getIndex() {
		return classIndex;
	}

	@Override
	public ClassDescriptor createInstance() {
		return new ClassDescriptor();
	}

	@Override
	public Map<RelationType, Set<? extends AbstractDescriptor>> getRelations(
			ClassDescriptor descriptor) {
		Map<RelationType, Set<? extends AbstractDescriptor>> relations = new HashMap<RelationType, Set<? extends AbstractDescriptor>>();
		relations.put(RelationType.CONTAINS, descriptor.getContains());
		relations.put(RelationType.DEPENDS_ON, descriptor.getDependencies());
		relations.put(RelationType.IMPLEMENTS, descriptor.getInterfaces());
		relations.put(RelationType.INHERITS_FROM,
				asSet(descriptor.getSuperClass()));
		return relations;
	}

	@Override
	protected void setRelation(ClassDescriptor descriptor,
			RelationType relation, AbstractDescriptor target) {
		switch (relation) {
		case CONTAINS:
			descriptor.getContains().add(target);
			break;
		case DEPENDS_ON:
			descriptor.getDependencies().add((ClassDescriptor) target);
			break;
		case IMPLEMENTS:
			descriptor.getInterfaces().add((ClassDescriptor) target);
			break;
		case INHERITS_FROM:
			descriptor.setSuperClass((ClassDescriptor) target);
			break;
		default:
			throw new IllegalArgumentException("Unsupported relation type "
					+ relation);
		}
	}

}
