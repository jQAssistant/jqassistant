package com.buschmais.jqassistant.store.impl.model;

import org.neo4j.graphdb.Node;

import com.buschmais.jqassistant.store.api.model.ClassDescriptor;
import com.buschmais.jqassistant.store.api.model.PackageDescriptor;

public class PackageDescriptorImpl extends AbstractDescriptor implements
		PackageDescriptor {

	private final PackageDescriptorImpl parent;

	public PackageDescriptorImpl(Node node, PackageDescriptorImpl parent) {
		super(node);
		this.parent = parent;
	}

	public PackageDescriptorImpl getParent() {
		return parent;
	}

	@Override
	public void addClassDescriptor(ClassDescriptor classDescriptor) {
		getNode().createRelationshipTo(getNode(classDescriptor),
				RelationType.CONTAINS);
	}

}
