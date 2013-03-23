package com.buschmais.jqassistant.store.impl.model;

import org.neo4j.graphdb.Node;

import com.buschmais.jqassistant.store.api.model.Descriptor;

public abstract class AbstractDescriptor implements Descriptor {

	private final Node node;

	public AbstractDescriptor(Node node) {
		this.node = node;
	}

	@Override
	public String getLocalName() {
		return (String) node.getProperty("localName");
	}

	@Override
	public void setLocalName(String localName) {
		node.setProperty("localName", localName);
	}

	@Override
	public String getFullQualifiedName() {
		return (String) node.getProperty("fullQualifiedName");
	}

	@Override
	public void setFullQualifiedName(String fullQualifiedName) {
		node.setProperty("fullQualifiedName", fullQualifiedName);
	}

	@Override
	public final String toString() {
		return getFullQualifiedName();
	}

	public Node getNode() {
		return node;
	}

	protected Node getNode(Descriptor descriptor) {
		return ((AbstractDescriptor) descriptor).getNode();
	}

}
