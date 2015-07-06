package com.buschmais.jqassistant.plugin.graphml.report.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import com.buschmais.xo.api.CompositeObject;

class VirtualRelationship implements Relationship {
    static long REL_ID = -1;

    private final long id;
    private final Node start;
    private final Node end;
    private final RelationshipType type;
    private final Map<String, Object> props = new LinkedHashMap<>();
    private final static String ROLE_RELATIONSHIP = "relationship";

    public static boolean isRelationship(Map m) {
        return ROLE_RELATIONSHIP.equals(m.get("role"));
    }

    public VirtualRelationship(Map m) {
        if (!isRelationship(m))
            throw new IllegalArgumentException("Not a relationship-map " + m);
        this.start = getDelegate(m.get("startNode"));
        this.end = getDelegate(m.get("endNode"));
        this.type = DynamicRelationshipType.withName((String) m.get("type"));
        this.id = m.containsKey("id") ? ((Number) m.get("id")).longValue() : REL_ID--;
        if (m.containsKey("properties")) {
            this.props.putAll((Map) m.get("properties"));
        }
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

    @Override
    public GraphDatabaseService getGraphDatabase() {
        return null;
    }

    @Override
    public boolean hasProperty(String key) {
        return props.containsKey(key);
    }

    @Override
    public Object getProperty(String key) {
        return props.get(key);
    }

    @Override
    public Object getProperty(String key, Object defaultValue) {
        return props.containsKey(key) ? props.get(key) : defaultValue;
    }

    @Override
    public void setProperty(String key, Object value) {
        props.put(key, value);
    }

    @Override
    public Object removeProperty(String key) {
        return props.remove(key);
    }

    @Override
    public Iterable<String> getPropertyKeys() {
        return props.keySet();
    }
}