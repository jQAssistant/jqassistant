package com.buschmais.jqassistant.core.report.api.graph;

import java.util.Collection;
import java.util.Map;

import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportHelper;
import com.buschmais.jqassistant.core.report.api.graph.model.Identifiable;
import com.buschmais.jqassistant.core.report.api.graph.model.Node;
import com.buschmais.jqassistant.core.report.api.graph.model.Relationship;
import com.buschmais.jqassistant.core.report.api.graph.model.SubGraph;
import com.buschmais.jqassistant.core.report.api.model.Column;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.xo.api.CompositeObject;
import com.buschmais.xo.neo4j.api.model.Neo4jLabel;
import com.buschmais.xo.neo4j.api.model.Neo4jNode;
import com.buschmais.xo.neo4j.api.model.Neo4jRelationship;

public class SubGraphFactory {

    private static final String ROLE = "role";
    private static final String NODE = "node";
    private static final String RELATIONSHIP = "relationship";
    private static final String GRAPH = "graph";

    private static final String LABEL = "label";

    private static final String PROPERTIES = "properties";
    private static final String LABELS = "labels";

    private static final String TYPE = "type";
    private static final String START_NODE = "startNode";
    private static final String END_NODE = "endNode";

    private static final String PARENT = "parent";
    private static final String NODES = "nodes";
    private static final String RELATIONSHIPS = "relationships";

    private long nodeId = -1;
    private long relationshipId = -1;
    private long subgraphId = -1;

    /**
     * Create a {@link SubGraph} from the given {@link Result}.
     *
     * @param result
     *            The result
     * @return The {@link SubGraph}.
     * @throws ReportException
     *             If the result contains an unsupported structure.
     */
    public SubGraph createSubGraph(Result<? extends ExecutableRule> result) throws ReportException {
        SubGraph graph = new SubGraph();
        graph.setId(subgraphId--);
        for (Row row : result.getRows()) {
            for (Column column : row.getColumns().values()) {
                addToValueGraph(graph, column.getValue());
            }
        }
        return graph;
    }

    /**
     * Convert a given value to an {@link Identifiable} element of a
     * {@link SubGraph}.
     *
     * @param value
     *            The value.
     * @return The {@link Identifiable}.
     * @throws ReportException
     *             If value that cannot be converted to an {@link Identifiable}.
     */
    public <I extends Identifiable> I toIdentifiable(Object value) throws ReportException {
        if (value == null) {
            return null;
        } else if (value instanceof Map) {
            Map<String, Object> virtualObject = (Map) value;
            Object role = virtualObject.get(ROLE);
            if (role != null) {
                Map<String, Object> properties = (Map<String, Object>) virtualObject.get(PROPERTIES);
                switch (role.toString().toLowerCase()) {
                    case NODE:
                        Node node = new Node();
                        node.setId(nodeId--);
                        Collection<String> labels = (Collection<String>) virtualObject.get(LABELS);
                        node.getLabels().addAll(labels);
                        node.getProperties().putAll(properties);
                        node.setLabel((String) virtualObject.get(LABEL));
                        return (I) node;
                    case RELATIONSHIP:
                        Relationship relationship = new Relationship();
                        relationship.setId(relationshipId--);
                        Node startNode = toIdentifiable(virtualObject.get(START_NODE));
                        Node endNode = toIdentifiable(virtualObject.get(END_NODE));
                        String type = (String) virtualObject.get(TYPE);
                        relationship.setType(type);
                        relationship.setStartNode(startNode);
                        relationship.setEndNode(endNode);
                        relationship.getProperties().putAll(properties);
                        relationship.setLabel((String) virtualObject.get(LABEL));
                        if (startNode == null || endNode == null || type == null) {
                            throw new ReportException("The virtual relationship does not contain either start node, end node or type: " + relationship);
                        }
                        return (I) relationship;
                    case GRAPH:
                        SubGraph subgraph = new SubGraph();
                        subgraph.setId(subgraphId--);
                        Node parent = toIdentifiable(virtualObject.get(PARENT));
                        subgraph.setParent(parent);
                        subgraph.setLabel((String) virtualObject.get(LABEL));
                        addToValueGraph(subgraph, virtualObject.get(NODES));
                        addToValueGraph(subgraph, virtualObject.get(RELATIONSHIPS));
                        return (I) subgraph;
                }
            }
        } else if (value instanceof CompositeObject) {
            CompositeObject compositeObject = (CompositeObject) value;
            Identifiable identifiable = toIdentifiable(compositeObject.getDelegate());
            identifiable.setLabel(ReportHelper.getLabel(value));
            return (I) identifiable;
        } else if (value instanceof Neo4jNode) {
            Neo4jNode<Neo4jLabel, ?, ?, ?> neo4jNode = (Neo4jNode<Neo4jLabel, ?, ?, ?>) value;
            Node node = new Node();
            node.setId(neo4jNode.getId());
            for (Neo4jLabel label : neo4jNode.getLabels()) {
                node.getLabels().add(label.getName());
            }
            node.getProperties().putAll(neo4jNode.getProperties());
            return (I) node;
        } else if (value instanceof Neo4jRelationship) {
            Neo4jRelationship<?, ?, ?, ?, ?> neo4jRelationship = (Neo4jRelationship) value;
            Relationship relationship = new Relationship();
            relationship.setId(neo4jRelationship.getId());
            relationship.setType(neo4jRelationship.getType().getName());
            relationship.setStartNode(toIdentifiable(neo4jRelationship.getStartNode()));
            relationship.setEndNode(toIdentifiable(neo4jRelationship.getEndNode()));
            relationship.getProperties().putAll(neo4jRelationship.getProperties());
            return (I) relationship;
        }
        throw new ReportException("Element type not supported: " + value);
    }

    private void addToValueGraph(SubGraph parent, Object value) throws ReportException {
        if (value != null) {
            if (value instanceof Iterable<?>) {
                Iterable<?> values = (Iterable<?>) value;
                for (Object singleValue : values) {
                    addToValueGraph(parent, singleValue);
                }
            } else {
                addIdentifiableToGraph(parent, toIdentifiable(value));
            }
        }
    }

    private void addIdentifiableToGraph(SubGraph parent, Identifiable identifiable) {
        if (identifiable != null) {
            if (identifiable instanceof Node) {
                parent.getNodes().put(identifiable.getId(), (Node) identifiable);
            } else if (identifiable instanceof Relationship) {
                parent.getRelationships().put(identifiable.getId(), (Relationship) identifiable);
            } else if (identifiable instanceof SubGraph) {
                parent.getSubGraphs().put(identifiable.getId(), (SubGraph) identifiable);
            }
        }
    }
}
