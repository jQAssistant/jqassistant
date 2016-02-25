package com.buschmais.jqassistant.plugin.graphml.report.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Traverser;
import org.neo4j.graphdb.Traverser.Order;

import com.buschmais.jqassistant.plugin.graphml.report.api.SubGraph;

public class VirtualNode extends VirtualPropertyContainer implements Node {

    private static long NODE_ID = -1;
    private static final String ROLE_NODE = "node";

    private long id;
    private final List<Relationship> relationships = new ArrayList<>();
    private final List<Label> labels = new ArrayList<>();
    private final Set<SubGraph> subgraphs = new LinkedHashSet<>();

    public static boolean isNode(Map<String, Object> m) {
        return ROLE_NODE.equals(m.get("role"));
    }

    public VirtualNode(Map<String, Object> m) {
        super(m);
        if (!isNode(m)) {
            throw new IllegalArgumentException("Not a node-map " + m);
        }
        this.id = m.containsKey("id") ? ((Number) m.get("id")).longValue() : NODE_ID--;

        if (m.containsKey("relationships")) {
            this.relationships.addAll((List<Relationship>) m.get("relationships"));
        }

        if (m.containsKey("labels")) {
            List<Object> labelList = (List<Object>) m.get("labels");
            for (Object label : labelList) {
                if (label instanceof Label) {
                    labels.add((Label) label);
                } else {
                    labels.add(DynamicLabel.label(label.toString()));
                }

            }
        }
    }

    public void add(Object o) {
        if (o instanceof SubGraphImpl) {
            subgraphs.add((SubGraph) o);
        } else if (o instanceof Iterable) {
            for (Object iterOb : (Iterable) o) {
                add(iterOb);
            }
        }
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void delete() {
    }

    @Override
    public Iterable<Relationship> getRelationships() {
        return relationships;
    }

    @Override
    public boolean hasRelationship() {
        return !relationships.isEmpty();
    }

    @Override
    public Iterable<Relationship> getRelationships(RelationshipType... types) {
        List<RelationshipType> relTypes = Arrays.asList(types);
        List<Relationship> returnRels = new ArrayList<>(relationships.size());
        for (Relationship relationship : relationships) {
            if (relTypes.contains(relationship.getType())) {
                returnRels.add(relationship);
            }
        }

        return returnRels;
    }

    @Override
    public Iterable<Relationship> getRelationships(Direction direction, RelationshipType... types) {
        List<RelationshipType> relTypes = Arrays.asList(types);
        List<Relationship> returnRels = new ArrayList<>(relationships.size());
        for (Relationship relationship : relationships) {
            // TODO use direction filter
            if (relTypes.contains(relationship.getType())) {
                returnRels.add(relationship);
            }
        }

        return null;
    }

    @Override
    public boolean hasRelationship(RelationshipType... types) {
        List<RelationshipType> relTypes = Arrays.asList(types);
        for (Relationship relationship : relationships) {
            if (relTypes.contains(relationship.getType())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean hasRelationship(Direction direction, RelationshipType... types) {
        List<RelationshipType> relTypes = Arrays.asList(types);
        for (Relationship relationship : relationships) {
            // TODO use direction filter
            if (relTypes.contains(relationship.getType())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Iterable<Relationship> getRelationships(Direction dir) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasRelationship(Direction dir) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Iterable<Relationship> getRelationships(RelationshipType type, Direction dir) {
        return getRelationships(dir, type);
    }

    @Override
    public boolean hasRelationship(RelationshipType type, Direction dir) {
        return hasRelationship(dir, type);
    }

    @Override
    public Relationship getSingleRelationship(RelationshipType type, Direction dir) {
        Iterable<Relationship> rels = getRelationships(type, dir);
        Iterator<Relationship> iterator = rels.iterator();
        if (iterator.hasNext()) {
            Relationship relationship = iterator.next();
            if (iterator.hasNext()) {
                throw new RuntimeException("More than one relationship with type=" + type + " and dir=" + dir + " on node " + this);
            }
            return relationship;
        }

        return null;
    }

    @Override
    public Relationship createRelationshipTo(Node otherNode, RelationshipType type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<RelationshipType> getRelationshipTypes() {
        Set<RelationshipType> types = new HashSet<>();
        for (Relationship relationship : relationships) {
            types.add(relationship.getType());
        }

        return types;
    }

    @Override
    public int getDegree() {
        return relationships.size();
    }

    @Override
    public int getDegree(RelationshipType type) {
        return ((List<Relationship>) getRelationships(type)).size();
    }

    @Override
    public int getDegree(Direction direction) {
        return ((List<Relationship>) getRelationships(direction)).size();
    }

    @Override
    public int getDegree(RelationshipType type, Direction direction) {
        return ((List<Relationship>) getRelationships(type, direction)).size();
    }

    @Override
    public Traverser traverse(Order traversalOrder, StopEvaluator stopEvaluator, ReturnableEvaluator returnableEvaluator, RelationshipType relationshipType,
            Direction direction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Traverser traverse(Order traversalOrder, StopEvaluator stopEvaluator, ReturnableEvaluator returnableEvaluator,
            RelationshipType firstRelationshipType, Direction firstDirection, RelationshipType secondRelationshipType, Direction secondDirection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Traverser traverse(Order traversalOrder, StopEvaluator stopEvaluator, ReturnableEvaluator returnableEvaluator,
            Object... relationshipTypesAndDirections) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addLabel(Label label) {
        labels.add(label);
    }

    @Override
    public void removeLabel(Label label) {
        labels.remove(label);
    }

    @Override
    public boolean hasLabel(Label label) {
        return labels.contains(label);
    }

    @Override
    public Iterable<Label> getLabels() {
        return labels;
    }

}
