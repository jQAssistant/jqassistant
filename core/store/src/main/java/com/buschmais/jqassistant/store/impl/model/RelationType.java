package com.buschmais.jqassistant.store.impl.model;

import org.neo4j.graphdb.RelationshipType;

public enum RelationType implements RelationshipType {

	DEPENDS_ON, CONTAINS, EXTENDS, IMPLEMENTS, THROWS;
}
