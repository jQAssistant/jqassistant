package com.buschmais.jqassistant.store.impl.model;

import org.neo4j.graphdb.Node;

import com.buschmais.jqassistant.store.api.model.Descriptor;

public abstract class AbstractParentDescriptor extends AbstractDescriptor {

	public AbstractParentDescriptor(Node node) {
		super(node);
	}

	public void addChild(Descriptor child) {
		addRelationShip(RelationType.CONTAINS, child);
	}

}
