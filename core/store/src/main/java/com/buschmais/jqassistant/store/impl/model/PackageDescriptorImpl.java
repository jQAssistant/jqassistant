package com.buschmais.jqassistant.store.impl.model;

import org.neo4j.graphdb.Node;

import com.buschmais.jqassistant.store.api.model.PackageDescriptor;

public class PackageDescriptorImpl extends AbstractParentDescriptor implements
		PackageDescriptor {

	public PackageDescriptorImpl(Node node) {
		super(node);
	}

}
