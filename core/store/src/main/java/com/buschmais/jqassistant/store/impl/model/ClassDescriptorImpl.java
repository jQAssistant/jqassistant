package com.buschmais.jqassistant.store.impl.model;

import org.neo4j.graphdb.Node;

import com.buschmais.jqassistant.store.api.model.ClassDescriptor;

public class ClassDescriptorImpl extends AbstractParentDescriptor implements
		ClassDescriptor {

	public ClassDescriptorImpl(Node node) {
		super(node);
	}

	@Override
	public void addDependency(ClassDescriptor dependency) {
		addRelationShip(RelationType.DEPENDS_ON, dependency);
	}

	@Override
	public void addSuperClass(ClassDescriptor superClass) {
		addRelationShip(RelationType.INHERITS_FROM, superClass);

	}

	@Override
	public void addImplements(ClassDescriptor interfaceClass) {
		addRelationShip(RelationType.IMPLEMENTS, interfaceClass);
	}

}
