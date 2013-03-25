package com.buschmais.jqassistant.store.impl.model;

import org.neo4j.graphdb.Node;

import com.buschmais.jqassistant.store.api.model.ClassDescriptor;
import com.buschmais.jqassistant.store.api.model.MethodDescriptor;

public class MethodDescriptorImpl extends AbstractDescriptor implements
		MethodDescriptor {

	private final ClassDescriptorImpl classDescriptor;

	public MethodDescriptorImpl(Node node, ClassDescriptorImpl classDescriptor) {
		super(node);
		this.classDescriptor = classDescriptor;
	}

	public ClassDescriptorImpl getClassDescriptor() {
		return classDescriptor;
	}

	@Override
	public void addDependency(ClassDescriptor dependency) {
		addRelationShip(RelationType.DEPENDS_ON, dependency);
	}
}
