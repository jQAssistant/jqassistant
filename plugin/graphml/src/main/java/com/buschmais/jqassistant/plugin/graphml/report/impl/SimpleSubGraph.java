package com.buschmais.jqassistant.plugin.graphml.report.impl;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.buschmais.xo.api.CompositeObject;

class SimpleSubGraph {

    private static final String ROLE_GRAPH = "graph";

    private Set<CompositeObject> nodes = new LinkedHashSet<>(1000);
    private Set<CompositeObject> relationships = new LinkedHashSet<>(1000);
    private CompositeObject parentNode;
    private Set<SimpleSubGraph> subgraphs = new LinkedHashSet<>(1000);

    public SimpleSubGraph() {
    }

    public SimpleSubGraph(Map m) {
        if (!isSubgraph(m))
            throw new IllegalArgumentException("the argument m (" + m + ") is not a subgraph map");

        if (m.containsKey("nodes"))
            add((List<Node>) m.get("nodes"));
        if (m.containsKey("relationships"))
            add((List<CompositeObject>) m.get("relationships"));
        if (m.containsKey("parent"))
            parentNode = (CompositeObject) m.get("parent");
    }

    public CompositeObject getParentNode() {
        return parentNode;
    }

    public Iterable<CompositeObject> getNodes() {
        return nodes;
    }

    /**
     * Gets all nodes including nodes from subgraphs AND the parent node.
     * 
     * @return a list of all nodes
     */
    public Collection<CompositeObject> getAllNodes() {
        Set<CompositeObject> allNodes = new LinkedHashSet<>(1000);
        if (parentNode != null) {
            allNodes.add(parentNode);
        }

        allNodes.addAll(nodes);
        for (SimpleSubGraph subgraph : subgraphs) {
            allNodes.addAll(subgraph.getAllNodes());
        }

        return allNodes;
    }

    public Iterable<CompositeObject> getRelationships() {
        return relationships;
    }

    /**
     * Gets all nodes including nodes from subgraphs.
     * 
     * @return a list of all nodes
     */
    public Collection<CompositeObject> getAllRelationships() {
        Set<CompositeObject> allRels = new LinkedHashSet<>(1000);
        allRels.addAll(relationships);
        for (SimpleSubGraph subgraph : subgraphs) {
            allRels.addAll(subgraph.getAllRelationships());
        }

        return allRels;
    }

    public void add(Object value) {
        if (value instanceof CompositeObject) {
            CompositeObject compositeObject = (CompositeObject) value;
            Object o = compositeObject.getDelegate();
            if (o instanceof Node) {
                nodes.add(compositeObject);
            } else if (o instanceof Relationship) {
                relationships.add(compositeObject);
            } else {
                add(o);
            }
        } else if (value instanceof SimpleSubGraph) {
            subgraphs.add((SimpleSubGraph) value);
        } else if (value instanceof Relationship) {
            Relationship rel = (Relationship) value;
            relationships.add(new RelationWrapper(rel));
        } else if (value instanceof Node) {
            Node node = (Node) value;
            nodes.add(new NodeWrapper(node));
        } else if (value instanceof Iterable) {
            for (Object o : (Iterable) value)
                add(o);
        }
    }

    public static boolean isSubgraph(Map m) {
        return ROLE_GRAPH.equals(m.get("role"));
    }

    public Set<SimpleSubGraph> getSubgraphs() {
        return subgraphs;
    }

    public boolean contains(Relationship relationship) {
        return relationships.contains(relationship);
    }

    @SuppressWarnings("unchecked")
    private class RelationWrapper implements CompositeObject {

        private Relationship rel;

        public RelationWrapper(Relationship rel) {
            this.rel = rel;
        }

        @Override
        public Long getId() {
            return Long.valueOf(rel.getId());
        }

        @Override
        public <T> T as(Class<T> type) {
            return null;
        }

        @Override
        public Relationship getDelegate() {
            return rel;
        }

        @Override
        public String toString() {
            return "VirtualRelationship[type=" + rel.getType().name() + "]";
        }

    }

    @SuppressWarnings("unchecked")
    private class NodeWrapper implements CompositeObject {

        private Node node;

        public NodeWrapper(Node node) {
            this.node = node;
        }

        @Override
        public Long getId() {
            return Long.valueOf(node.getId());
        }

        @Override
        public <T> T as(Class<T> type) {
            return null;
        }

        @Override
        public Node getDelegate() {
            return node;
        }

        @Override
        public String toString() {
            return "VirtualNode[labels=" + MetaInformation.getLabelsString(node) + "]";
        }
    }

}