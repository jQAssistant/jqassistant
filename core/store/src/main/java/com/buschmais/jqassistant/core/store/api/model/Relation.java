package com.buschmais.jqassistant.core.store.api.model;

import org.neo4j.graphdb.RelationshipType;

import java.util.HashMap;
import java.util.Map;

public enum Relation implements RelationshipType {

    ANNOTATED_BY, DEPENDS_ON, CONTAINS, EXTENDS, IMPLEMENTS, THROWS, HAS, OF_TYPE;

    private static Map<String, Relation> relations;

    static {
        relations = new HashMap<>();
        for (Relation relation : Relation.values()) {
            relations.put(relation.name(), relation);
        }
    }

    public static Relation getRelation(String name) {
        return relations.get(name);
    }
}
