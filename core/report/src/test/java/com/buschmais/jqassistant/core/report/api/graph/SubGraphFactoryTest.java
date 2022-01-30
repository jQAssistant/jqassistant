package com.buschmais.jqassistant.core.report.api.graph;

import java.util.Map;
import java.util.stream.Stream;

import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.graph.model.Node;
import com.buschmais.jqassistant.core.report.api.graph.model.Relationship;
import com.buschmais.jqassistant.core.report.api.graph.model.SubGraph;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.shared.map.MapBuilder;
import com.buschmais.xo.api.CompositeObject;
import com.buschmais.xo.neo4j.api.model.Neo4jLabel;
import com.buschmais.xo.neo4j.api.model.Neo4jNode;
import com.buschmais.xo.neo4j.api.model.Neo4jRelationship;
import com.buschmais.xo.neo4j.api.model.Neo4jRelationshipType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class SubGraphFactoryTest {

    private SubGraphFactory factory = new SubGraphFactory();

    @Test
    public void nodeAndRelationship() throws ReportException {
        MapBuilder<String, Object> builder = MapBuilder.builder();

        Map<String, Object> nodeProperties = MapBuilder.<String, Object> builder().entry("nodeKey", "value").build();
        builder.entry("node", getNeo4jNode(1l, nodeProperties, "Test1", "Test2"));
        Map<String, Object> relationshipProperties = MapBuilder.<String, Object> builder().entry("relationshipKey", "value").build();
        builder.entry("relation", getNeo4jRelationship(1l, relationshipProperties, "TEST"));
        Result<ExecutableRule> result = Result.builder().rows(singletonList(builder.build())).build();

        SubGraph graph = factory.createSubGraph(result);

        assertThat(graph.getId()).isEqualTo(-1l);
        assertThat(graph.getParent()).isNull();
        assertThat(graph.getSubGraphs()).isEmpty();

        Map<Long, Node> nodes = graph.getNodes();
        assertThat(nodes).hasSize(1);
        Node node = nodes.get(1l);
        assertThat(node.getId()).isEqualTo(1l);
        assertThat(node.getLabels()).containsExactly("Test1", "Test2");
        assertThat(node.getProperties()).isEqualTo(nodeProperties);

        Map<Long, Relationship> relationships = graph.getRelationships();
        assertThat(relationships).hasSize(1);
        Relationship relationship = relationships.get(1l);
        assertThat(relationship.getId()).isEqualTo(1l);
        assertThat(relationship.getType()).isEqualTo("TEST");
        assertThat(relationship.getProperties()).isEqualTo(relationshipProperties);
    }

    @Test
    public void collectionOfNodesAndCollectionOfRelationships() throws ReportException {
        MapBuilder<String, Object> builder = MapBuilder.builder();

        builder.entry("nodes", asList(asList(getNeo4jNode(1l), getNeo4jNode(2l))));
        builder.entry("relations", asList(asList(getNeo4jRelationship(1l, "TEST"), getNeo4jRelationship(2l, "TEST"))));
        Result<ExecutableRule> result = Result.builder().rows(singletonList(builder.build())).build();

        SubGraph graph = factory.createSubGraph(result);

        assertThat(graph.getId()).isEqualTo(-1l);
        assertThat(graph.getParent()).isNull();
        assertThat(graph.getSubGraphs()).isEmpty();

        Map<Long, Node> nodes = graph.getNodes();
        assertThat(nodes).hasSize(2);
        Node node1 = nodes.get(1l);
        assertThat(node1.getId()).isEqualTo(1l);
        Node node2 = nodes.get(2l);
        assertThat(node2.getId()).isEqualTo(2l);

        Map<Long, Relationship> relationships = graph.getRelationships();
        assertThat(relationships).hasSize(2);
        Relationship relationship1 = relationships.get(1l);
        assertThat(relationship1.getId()).isEqualTo(1l);
        Relationship relationship2 = relationships.get(2l);
        assertThat(relationship2.getId()).isEqualTo(2l);
    }

    @Test
    public void virtualNode() throws ReportException {
        Map<Object, Object> properties = MapBuilder.builder().entry("key", "value").build();
        Map<String, Object> virtualNode = MapBuilder.<String, Object> builder() //
                .entry("role", "node") //
                .entry("label", "Virtual Node") //
                .entry("labels", singletonList("Test")) //
                .entry("properties", properties) //
                .build();
        MapBuilder<String, Object> builder = MapBuilder.builder();
        builder.entry("nodes", singletonList(virtualNode));
        Result<ExecutableRule> result = Result.builder().rows(singletonList(builder.build())).build();

        SubGraph graph = factory.createSubGraph(result);

        Map<Long, Node> nodes = graph.getNodes();
        assertThat(nodes.size()).isEqualTo(1);
        Node node = nodes.get(-1l);
        assertThat(node).isNotNull();
        assertThat(node.getId()).isEqualTo(-1l);
        assertThat(node.getLabel()).isEqualTo("Virtual Node");
        assertThat(node.getLabels()).containsExactly("Test");
        assertThat(node.getProperties()).isEqualTo(properties);
    }

    @Test
    public void virtualRelationship() throws ReportException {
        Map<Object, Object> properties = MapBuilder.builder().entry("key", "value").build();
        Map<String, Object> virtualNode = MapBuilder.<String, Object> builder() //
                .entry("role", "relationship") //
                .entry("label", "Virtual Relationship") //
                .entry("type", "TEST") //
                .entry("properties", properties) //
                .entry("startNode", getNeo4jNode(1l)) //
                .entry("endNode", getNeo4jNode(2l)) //
                .build();
        MapBuilder<String, Object> builder = MapBuilder.builder();
        builder.entry("relationships", singletonList(virtualNode));
        Result<ExecutableRule> result = Result.builder().rows(singletonList(builder.build())).build();

        SubGraph graph = factory.createSubGraph(result);

        Map<Long, Relationship> relationships = graph.getRelationships();
        assertThat(relationships.size()).isEqualTo(1);
        Relationship relationship = relationships.get(-1l);
        assertThat(relationship).isNotNull();
        assertThat(relationship.getId()).isEqualTo(-1l);
        assertThat(relationship.getLabel()).isEqualTo("Virtual Relationship");
        assertThat(relationship.getType()).isEqualTo("TEST");
        assertThat(relationship.getProperties()).isEqualTo(properties);
        Node startNode = relationship.getStartNode();
        assertThat(startNode).isNotNull();
        assertThat(startNode.getId()).isEqualTo(1l);
        Node endNode = relationship.getEndNode();
        assertThat(endNode).isNotNull();
        assertThat(endNode.getId()).isEqualTo(2l);
    }

    @Test
    public void subGraph() throws ReportException {
        Map<String, Object> virtualGraph = MapBuilder.<String, Object> builder() //
                .entry("role", "graph") //
                .entry("label", "Virtual Graph") //
                .entry("parent", getNeo4jNode(0l)) //
                .entry("nodes", singletonList(getNeo4jNode(1l))) //
                .entry("relationships", singletonList(getNeo4jRelationship(1l, "TEST"))) //
                .build();

        MapBuilder<String, Object> rowBuilder = MapBuilder.builder();
        Map<String, Object> row = rowBuilder.entry("graph", virtualGraph).build();

        Result<ExecutableRule> result = Result.builder().rows(singletonList(row)).build();

        SubGraph graph = factory.createSubGraph(result);

        assertThat(graph.getParent()).isNull();
        assertThat(graph.getNodes()).isEmpty();
        assertThat(graph.getRelationships()).isEmpty();

        Map<Long, SubGraph> subGraphs = graph.getSubGraphs();
        assertThat(subGraphs).hasSize(1);
        SubGraph subGraph = subGraphs.get(-2l);
        assertThat(subGraph.getId()).isEqualTo(-2l);
        assertThat(subGraph.getLabel()).isEqualTo("Virtual Graph");
        Node parent = subGraph.getParent();
        assertThat(parent).isNotNull();
        assertThat(parent.getId()).isEqualTo(0l);

        Map<Long, Node> nodes = subGraph.getNodes();
        assertThat(nodes).hasSize(1);
        Node node1 = nodes.get(1l);
        assertThat(node1.getId()).isEqualTo(1l);

        Map<Long, Relationship> relationships = subGraph.getRelationships();
        assertThat(relationships).hasSize(1);
        Relationship relationship1 = relationships.get(1l);
        assertThat(relationship1.getId()).isEqualTo(1l);
    }

    @Test
    public void compositeObject() throws ReportException {
        CompositeObject compositeObject = mock(CompositeObject.class);
        Neo4jNode neo4jNode = getNeo4jNode(1l, "Test");
        doReturn(neo4jNode).when(compositeObject).getDelegate();
        MapBuilder<String, Object> builder = MapBuilder.builder();
        builder.entry("nodes", singletonList(compositeObject));
        Result<ExecutableRule> result = Result.builder().rows(singletonList(builder.build())).build();

        SubGraph graph = factory.createSubGraph(result);

        Map<Long, Node> nodes = graph.getNodes();
        assertThat(nodes).isNotEmpty();
        Node node = nodes.get(1l);
        assertThat(node).isNotNull();
        assertThat(node.getId()).isEqualTo(1l);
        assertThat(node.getLabels()).containsExactly("Test");
    }

    private Neo4jNode getNeo4jNode(long id, String... labels) {
        return getNeo4jNode(id, emptyMap(), labels);
    }

    private Neo4jNode getNeo4jNode(long id, Map<String, Object> properties, String... labels) {
        Neo4jNode node = mock(Neo4jNode.class);
        doReturn(id).when(node).getId();
        doReturn(properties).when(node).getProperties();
        doReturn(Stream.of(labels).map(label -> (Neo4jLabel) () -> label).collect(toList())).when(node).getLabels();
        return node;
    }

    private Neo4jRelationship getNeo4jRelationship(long id, String type) {
        return getNeo4jRelationship(id, emptyMap(), type);
    }

    private Neo4jRelationship getNeo4jRelationship(long id, Map<String, Object> properties, String type) {
        Neo4jRelationship relationship = mock(Neo4jRelationship.class);
        doReturn(id).when(relationship).getId();
        doReturn(properties).when(relationship).getProperties();
        doReturn((Neo4jRelationshipType) () -> type).when(relationship).getType();
        return relationship;
    }
}
