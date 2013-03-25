package com.buschmais.jqassistant.store.impl.model;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.store.api.model.Descriptor;

public abstract class AbstractDescriptor implements Descriptor {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AbstractDescriptor.class);

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
		return (String) node.getProperty(FULLQUALIFIEDNAME);
	}

	@Override
	public void setFullQualifiedName(String fullQualifiedName) {
		node.setProperty(FULLQUALIFIEDNAME, fullQualifiedName);
	}

	@Override
	public final String toString() {
		return getFullQualifiedName();
	}

	public Node getNode() {
		return node;
	}

	protected void addRelationShip(RelationType type, Descriptor descriptor) {
		Node descriptorNode = getNode(descriptor);
		for (Relationship relationship : node.getRelationships(type,
				Direction.OUTGOING)) {
			if (type.name().equals(relationship.getType().name())
					&& node.equals(descriptorNode)) {
				return;
			}
		}
		LOGGER.info("Creating relationship '(" + this + ")-[:" + type + "]->("
				+ descriptor + ")'.");
		node.createRelationshipTo(descriptorNode, type);
	}

	private Node getNode(Descriptor descriptor) {
		return ((AbstractDescriptor) descriptor).getNode();
	}
}
