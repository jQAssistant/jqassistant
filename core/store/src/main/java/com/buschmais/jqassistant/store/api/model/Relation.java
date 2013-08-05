package com.buschmais.jqassistant.store.api.model;

import org.neo4j.graphdb.RelationshipType;

import java.util.HashMap;
import java.util.Map;

public enum Relation implements RelationshipType {

    ANNOTATED_BY, DEPENDS_ON, CONTAINS, EXTENDS, IMPLEMENTS, THROWS;

    private static Map<String, Relation> relations;

    static {
        relations = new HashMap<String, Relation>();
        for (Relation relation : Relation.values()) {
            relations.put(relation.name(), relation);
        }
    }

    public static Relation getRelation(String name) {
        return relations.get(name);
    }
}
