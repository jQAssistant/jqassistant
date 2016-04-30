package com.buschmais.jqassistant.plugin.graphml.report.impl;


import java.util.Map;

import com.buschmais.xo.api.CompositeObject;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

class VirtualRelationship extends VirtualPropertyContainer implements Relationship {
    static long REL_ID = -1;

    private final long id;
    private final Node start;
    private final Node end;
    private final RelationshipType type;
    private final static String ROLE_RELATIONSHIP = "relationship";

    public static boolean isRelationship(Map<String, Object> m) {
        return ROLE_RELATIONSHIP.equals(m.get("role"));
    }

    public VirtualRelationship(Map<String, Object> m) {
        super(m);
        if (!isRelationship(m))
            throw new IllegalArgumentException("Not a relationship-map " + m);
        this.start = getDelegate(m.get("startNode"));
        this.end = getDelegate(m.get("endNode"));
        this.type = DynamicRelationshipType.withName((String) m.get("type"));
        this.id = m.containsKey("id") ? ((Number) m.get("id")).longValue() : REL_ID--;
    }

    private Node getDelegate(Object object) {
        if (object instanceof Node) {
            return (Node) object;
        } else if (object instanceof CompositeObject) {
            return ((CompositeObject) object).getDelegate();
        }
        return null;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void delete() {
    }

    @Override
    public Node getStartNode() {
        return start;
    }

    @Override
    public Node getEndNode() {
        return end;
    }

    @Override
    public Node getOtherNode(Node node) {
        if (node.equals(start))
            return end;
        if (node.equals(end))
            return start;
        throw new IllegalArgumentException("Node is neither start nor end-node " + node);
    }

    @Override
    public Node[] getNodes() {
        return new Node[] { start, end };
    }

    @Override
    public RelationshipType getType() {
        return type;
    }

    @Override
    public boolean isType(RelationshipType type) {
        return type.name().equals(this.type.name());
    }

}
