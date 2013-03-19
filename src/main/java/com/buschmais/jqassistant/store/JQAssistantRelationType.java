package com.buschmais.jqassistant.store;

import org.neo4j.graphdb.RelationshipType;

public enum JQAssistantRelationType implements RelationshipType {

    DEPENDS_ON, CONTAINS;
}
