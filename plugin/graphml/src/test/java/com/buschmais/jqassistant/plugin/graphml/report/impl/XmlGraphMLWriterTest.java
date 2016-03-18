package com.buschmais.jqassistant.plugin.graphml.report.impl;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Report;
import com.buschmais.jqassistant.core.shared.reflection.ClassHelper;
import com.buschmais.jqassistant.core.store.api.model.SubGraph;
import com.buschmais.jqassistant.plugin.graphml.report.api.GraphMLDecorator;
import com.buschmais.jqassistant.plugin.graphml.report.decorator.YedGraphMLDecorator;
import com.buschmais.jqassistant.plugin.graphml.test.CustomGraphMLDecorator;
import com.buschmais.xo.api.CompositeObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class XmlGraphMLWriterTest {

    @Mock
    private ClassHelper classHelper;

    @Mock
    private Concept concept;

    @Mock
    private Result<?> result;

    @Mock
    private CompositeObject nodeObject1;

    @Mock
    private CompositeObject nodeObject2;

    @Mock
    private CompositeObject relationshipObject1;

    @Mock
    private CompositeObject relationshipObject2;

    @Before
    public void setUp() {
        when(result.getRule()).thenReturn(concept);

        Node node1 = stubNode();
        when(nodeObject1.getId()).thenReturn(1L);
        when(nodeObject1.getDelegate()).thenReturn(node1);
        Relationship relationship1 = stubRelationship(node1);
        when(relationshipObject1.getId()).thenReturn(1L);
        when(relationshipObject1.getDelegate()).thenReturn(relationship1);
        Node node2 = stubNode();
        when(nodeObject2.getId()).thenReturn(2L);
        when(nodeObject2.getDelegate()).thenReturn(node2);
        when(relationshipObject2.getId()).thenReturn(2L);
        Relationship relationship2 = stubRelationship(node2);
        when(relationshipObject2.getDelegate()).thenReturn(relationship2);
    }

    private Relationship stubRelationship(Node node) {
        Relationship relationship = mock(Relationship.class);
        when(relationship.getType()).thenReturn(DynamicRelationshipType.withName("Test"));
        when(relationship.getPropertyKeys()).thenReturn(Collections.<String>emptyList());
        when(relationship.getStartNode()).thenReturn(node);
        when(relationship.getEndNode()).thenReturn(node);
        return relationship;
    }

    private Node stubNode() {
        Node node = mock(Node.class);
        when(node.getPropertyKeys()).thenReturn(Collections.<String>emptyList());
        when(node.getLabels()).thenReturn(Collections.<Label>emptyList());
        return node;
    }

    @Test
    public void ruleSpecificDecorator() throws IOException, XMLStreamException {
        Report report = Report.Builder.newInstance().property("graphml.report.decorator", CustomGraphMLDecorator.class.getName()).get();
        stubDecorator(report, CustomGraphMLDecorator.class);
        File file = getFile();
        Map<String, Object> properties = new HashMap<>();
        XmlGraphMLWriter writer = new XmlGraphMLWriter(classHelper, YedGraphMLDecorator.class, properties);
        SubGraph subGraph = getSubGraph();

        writer.write(result, subGraph, file);

        verify(classHelper).getType(CustomGraphMLDecorator.class.getName());
        verify(classHelper).createInstance(CustomGraphMLDecorator.class);
    }

    @Test
    public void defaultDecorator() throws IOException, XMLStreamException {
        Report report = Report.Builder.newInstance().get();
        stubDecorator(report, YedGraphMLDecorator.class);
        File file = getFile();
        Map<String, Object> properties = new HashMap<>();
        XmlGraphMLWriter writer = new XmlGraphMLWriter(classHelper, YedGraphMLDecorator.class, properties);
        SubGraph subGraph = getSubGraph();

        writer.write(result, subGraph, file);

        verify(classHelper).createInstance(YedGraphMLDecorator.class);
    }


    @Test
    public void decoratorFilter() throws IOException, XMLStreamException {
        Report report = Report.Builder.newInstance().get();
        YedGraphMLDecorator decorator = stubDecorator(report, YedGraphMLDecorator.class);
        when(decorator.isWriteNode(nodeObject1)).thenReturn(true);
        when(decorator.isWriteRelationship(relationshipObject1)).thenReturn(true);
        when(decorator.isWriteNode(nodeObject2)).thenReturn(false);
        when(decorator.isWriteRelationship(relationshipObject2)).thenReturn(false);
        File file = getFile();
        Map<String, Object> properties = new HashMap<>();
        XmlGraphMLWriter writer = new XmlGraphMLWriter(classHelper, YedGraphMLDecorator.class, properties);
        SubGraph subGraph = getSubGraph();

        writer.write(result, subGraph, file);

        verify(decorator).isWriteNode(nodeObject1);
        verify(decorator).writeNodeAttributes(nodeObject1);
        verify(decorator).writeNodeElements(nodeObject1);
        verify(decorator).isWriteRelationship(relationshipObject1);
        verify(decorator).writeRelationshipAttributes(relationshipObject1);
        verify(decorator).writeRelationshipElements(relationshipObject1);

        verify(decorator).isWriteNode(nodeObject2);
        verify(decorator, never()).writeNodeAttributes(nodeObject2);
        verify(decorator, never()).writeNodeElements(nodeObject2);
        verify(decorator).isWriteRelationship(relationshipObject2);
        verify(decorator, never()).writeRelationshipAttributes(relationshipObject2);
        verify(decorator, never()).writeRelationshipElements(relationshipObject2);
    }

    private <T extends GraphMLDecorator> T stubDecorator(Report report, Class<T> decoratorClass) {
        when(concept.getReport()).thenReturn(report);
        T decorator = mock(decoratorClass);
        doReturn(decoratorClass).when(classHelper).getType(decoratorClass.getName());
        doReturn(decorator).when(classHelper).createInstance(decoratorClass);
        return decorator;
    }

    private File getFile() throws IOException {
        File file = File.createTempFile("test", ".graphml");
        file.deleteOnExit();
        return file;
    }

    private SubGraph getSubGraph() {
        SubGraphImpl subGraph = new SubGraphImpl();
        subGraph.add(nodeObject1);
        subGraph.add(nodeObject2);
        subGraph.add(relationshipObject1);
        subGraph.add(relationshipObject2);
        return subGraph;
    }
}
