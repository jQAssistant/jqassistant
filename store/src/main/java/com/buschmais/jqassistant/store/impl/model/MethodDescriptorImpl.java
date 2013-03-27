package com.buschmais.jqassistant.store.impl.model;

import org.neo4j.graphdb.Node;

import com.buschmais.jqassistant.store.api.model.ClassDescriptor;
import com.buschmais.jqassistant.store.api.model.MethodDescriptor;

public class MethodDescriptorImpl extends AbstractDescriptor implements
		MethodDescriptor {

	public MethodDescriptorImpl(Node node) {
		super(node);
	}

	@Override
	public void addDependency(ClassDescriptor dependency) {
		addRelationShip(RelationType.DEPENDS_ON, dependency);
	}

	@Override
	public void addThrows(ClassDescriptor exception) {
		addRelationShip(RelationType.THROWS, exception);		
	}
}
