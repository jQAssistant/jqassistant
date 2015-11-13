package com.buschmais.jqassistant.plugin.graphml.report.impl;

import java.util.*;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.buschmais.xo.api.CompositeObject;

class SimpleSubGraph {

    private static final String ROLE_GRAPH = "graph";

    private CompositeObject parentNode;
    private Map<Long, SimpleSubGraph> subgraphs = new LinkedHashMap<>(1000);
    private Map<Long, CompositeObject> nodes = new LinkedHashMap<>(1000);
    private Map<Long, CompositeObject> relationships = new LinkedHashMap<>(1000);

    public SimpleSubGraph() {
    }

    public SimpleSubGraph(Map m) {
        if (!isSubgraph(m))
            throw new IllegalArgumentException("the argument m (" + m + ") is not a subgraph map");
        if (m.containsKey("nodes"))
            add(m.get("nodes"));
        if (m.containsKey("relationships"))
            add(m.get("relationships"));
        if (m.containsKey("parent"))
            parentNode = (CompositeObject) m.get("parent");
    }

    public CompositeObject getParentNode() {
        return parentNode;
    }

    public Iterable<CompositeObject> getNodes() {
        return nodes.values();
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

        allNodes.addAll(nodes.values());
        for (SimpleSubGraph subgraph : subgraphs.values()) {
            allNodes.addAll(subgraph.getAllNodes());
        }

        return allNodes;
    }

    /**
     * Gets all nodes including nodes from subgraphs.
     *
     * @return a list of all nodes
     */
    public Collection<CompositeObject> getAllRelationships() {
        Set<CompositeObject> allRels = new LinkedHashSet<>(1000);
        allRels.addAll(relationships.values());
        for (SimpleSubGraph subgraph : subgraphs.values()) {
            allRels.addAll(subgraph.getAllRelationships());
        }
        return allRels;
    }

    public void add(Object value) {
        if (value instanceof CompositeObject) {
            CompositeObject compositeObject = (CompositeObject) value;
            Long id = compositeObject.getId();
            Object o = compositeObject.getDelegate();
            if (o instanceof Node) {
                nodes.put(id, compositeObject);
            } else if (o instanceof Relationship) {
                relationships.put(id, compositeObject);
            } else {
                add(o);
            }
        } else if (value instanceof SimpleSubGraph) {
            SimpleSubGraph subGraph = (SimpleSubGraph) value;
            subgraphs.put(subGraph.getId(), subGraph);
        } else if (value instanceof Relationship) {
            Relationship rel = (Relationship) value;
            relationships.put(rel.getId(), new RelationWrapper(rel));
        } else if (value instanceof Node) {
            Node node = (Node) value;
            nodes.put(node.getId(), new NodeWrapper(node));
        } else if (value instanceof Iterable) {
            for (Object o : (Iterable) value)
                add(o);
        }
    }

    public static boolean isSubgraph(Map m) {
        return ROLE_GRAPH.equals(m.get("role"));
    }

    public Collection<SimpleSubGraph> getSubgraphs() {
        return subgraphs.values();
    }

    public Long getId() {
        return parentNode.getId();
    }

    @SuppressWarnings("unchecked")
    private class RelationWrapper implements CompositeObject {

        private Relationship rel;

        public RelationWrapper(Relationship rel) {
            this.rel = rel;
        }

        @Override
        public Long getId() {
            return rel.getId();
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
            return node.getId();
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