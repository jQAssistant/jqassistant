package com.buschmais.jqassistant.store.impl.model;

import org.neo4j.graphdb.Node;

import com.buschmais.jqassistant.store.api.model.Descriptor;
import com.buschmais.jqassistant.store.api.model.ParentDescriptor;

public abstract class AbstractParentDescriptor extends AbstractDescriptor implements ParentDescriptor {

	public AbstractParentDescriptor(Node node) {
		super(node);
	}

	@Override
	public void addChild(Descriptor child) {
		addRelationShip(RelationType.CONTAINS, child);
	}

}
