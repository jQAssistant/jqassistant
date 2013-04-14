package com.buschmais.jqassistant.store.impl.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.store.api.model.Descriptor;

public abstract class AbstractDescriptor implements Descriptor {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AbstractDescriptor.class);

	private final Node node;

	private final Map<RelationType, Set<AbstractDescriptor>> relationCache = new HashMap<RelationType, Set<AbstractDescriptor>>();

	public AbstractDescriptor(Node node) {
		this.node = node;
	}

	@Override
	public String getFullQualifiedName() {
		return (String) node.getProperty(FULLQUALIFIEDNAME);
	}

	public void setFullQualifiedName(String fullQualifiedName) {
		node.setProperty(FULLQUALIFIEDNAME, fullQualifiedName);
	}

	@Override
	public NodeType getType() {
		return NodeType.valueOf(node.getProperty(TYPE).toString());
	}

	public void setType(NodeType type) {
		node.setProperty(TYPE, type.name());
	}

	@Override
	public final String toString() {
		return getFullQualifiedName();
	}

	public Node getNode() {
		return node;
	}

	protected void addRelationShip(RelationType type, Descriptor descriptor) {
		if (!this.equals(descriptor)) {
			Set<AbstractDescriptor> relationsPerType = relationCache.get(type);
			if (relationsPerType == null) {
				relationsPerType = new HashSet<AbstractDescriptor>();
				relationCache.put(type, relationsPerType);
			}
			if (!relationsPerType.contains(descriptor)) {
				Node descriptorNode = getNode(descriptor);
				LOGGER.debug("Creating relationship '(" + this + ")-[:" + type
						+ "]->(" + descriptor + ")'.");
				node.createRelationshipTo(descriptorNode, type);
			}
		}
	}

	private Node getNode(Descriptor descriptor) {
		return ((AbstractDescriptor) descriptor).getNode();
	}
}
