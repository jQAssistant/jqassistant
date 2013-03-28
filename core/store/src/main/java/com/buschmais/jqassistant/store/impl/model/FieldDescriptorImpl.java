package com.buschmais.jqassistant.store.impl.model;

import org.neo4j.graphdb.Node;

import com.buschmais.jqassistant.store.api.model.ClassDescriptor;
import com.buschmais.jqassistant.store.api.model.FieldDescriptor;

public class FieldDescriptorImpl extends AbstractParentDescriptor implements
		FieldDescriptor {

	public FieldDescriptorImpl(Node node) {
		super(node);
	}

	@Override
	public void addDependency(ClassDescriptor dependency) {
		addRelationShip(RelationType.DEPENDS_ON, dependency);
	}

}
