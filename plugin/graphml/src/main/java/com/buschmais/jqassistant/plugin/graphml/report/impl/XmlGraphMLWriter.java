package com.buschmais.jqassistant.plugin.graphml.report.impl;

import com.buschmais.jqassistant.core.analysis.api.rule.Rule;
import com.buschmais.jqassistant.plugin.graphml.report.api.GraphMLDecorator;
import com.buschmais.xo.api.CompositeObject;
import com.sun.xml.txw2.output.IndentingXMLStreamWriter;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

import static com.buschmais.jqassistant.plugin.graphml.report.impl.MetaInformation.getLabelsString;

/**
 * @author mh
 * @since 21.01.14
 */
public class XmlGraphMLWriter {

    private XMLOutputFactory xmlOutputFactory;

    private GraphMLDecorator decorator;

    /**
     * Constructor.
     *
     * @param decorator The decorator to use.
     */
    XmlGraphMLWriter(GraphMLDecorator decorator) {
        this.decorator = decorator;
        xmlOutputFactory = XMLOutputFactory.newInstance();
    }

    void write(Rule rule, SimpleSubGraph graph, Writer writer) throws IOException, XMLStreamException {
        Collection<CompositeObject> allCoNodes = graph.getAllNodes();
        XMLStreamWriter xmlWriter = new IndentingXMLStreamWriter(xmlOutputFactory.createXMLStreamWriter(writer));
        GraphMLNamespaceContext context = new GraphMLNamespaceContext(decorator.getNamespaces());
        xmlWriter.setNamespaceContext(context);
        writeHeader(xmlWriter, context);
        writeKeyTypes(xmlWriter, graph);
        decorator.writeKeys(xmlWriter);
        writeSubgraph(rule, graph, xmlWriter);

        // filter and write edges
        Set<Long> allNodes = new HashSet<>();
        for (CompositeObject compositeObject : allCoNodes) {
            allNodes.add(((Node) compositeObject.getDelegate()).getId());
        }

        for (CompositeObject coRel : graph.getAllRelationships()) {
            Relationship rel = coRel.getDelegate();
            long startId = rel.getStartNode().getId();
            long endId = rel.getEndNode().getId();
            if (allNodes.contains(startId) && allNodes.contains(endId)) {
                writeRelationship(xmlWriter, coRel);
            }
        }

        writeFooter(xmlWriter);
    }

    private void writeSubgraph(Rule rule, SimpleSubGraph graph, XMLStreamWriter writer) throws XMLStreamException, IOException {
        CompositeObject wrapperNode = graph.getParentNode();
        if (wrapperNode != null) {
            writeNode(writer, rule, wrapperNode, false);
        }

        writer.writeStartElement("graph");
        writer.writeAttribute("id", "G" + graph.hashCode());
        writer.writeAttribute("edgedefault", "directed");
        newLine(writer);

        for (CompositeObject node : graph.getNodes()) {
            writeNode(writer, rule, node, true);
        }

        for (SimpleSubGraph subgraph : graph.getSubgraphs()) {
            writeSubgraph(rule, subgraph, writer);
        }

        endElement(writer);

        if (wrapperNode != null) {
            writer.writeEndElement();
        }
    }

    private void writeKeyTypes(XMLStreamWriter writer, SimpleSubGraph ops) throws IOException, XMLStreamException {
        Map<String, Class> keyTypes = new HashMap<>();
        keyTypes.put("labels", String.class);
        for (CompositeObject node : ops.getAllNodes()) {
            updateKeyTypes(keyTypes, node);
        }
        writeKeyTypes(writer, keyTypes, "node");
        keyTypes.clear();
        for (CompositeObject rel : ops.getAllRelationships()) {
            updateKeyTypes(keyTypes, rel);
        }
        writeKeyTypes(writer, keyTypes, "edge");
    }

    private void writeKeyTypes(XMLStreamWriter writer, Map<String, Class> keyTypes, String forType) throws IOException, XMLStreamException {
        for (Map.Entry<String, Class> entry : keyTypes.entrySet()) {
            String type = MetaInformation.typeFor(entry.getValue(), MetaInformation.GRAPHML_ALLOWED);
            if (type == null)
                continue;
            writer.writeEmptyElement("key");
            writer.writeAttribute("id", entry.getKey());
            writer.writeAttribute("for", forType);
            writer.writeAttribute("attr.name", entry.getKey());
            writer.writeAttribute("attr.type", type);
            newLine(writer);
        }
    }

    private void updateKeyTypes(Map<String, Class> keyTypes, CompositeObject composite) {
        PropertyContainer pc = composite.getDelegate();
        updateKeyTypes(keyTypes, pc);
    }

    private void updateKeyTypes(Map<String, Class> keyTypes, PropertyContainer pc) {
        for (String prop : pc.getPropertyKeys()) {
            Object value = pc.getProperty(prop);
            Class storedClass = keyTypes.get(prop);
            if (storedClass == null) {
                keyTypes.put(prop, value.getClass());
                continue;
            }
            if (storedClass == void.class || storedClass.equals(value.getClass()))
                continue;
            keyTypes.put(prop, void.class);
        }
    }

    private int writeNode(XMLStreamWriter writer, Rule rule, CompositeObject composite, boolean withEnd) throws IOException, XMLStreamException {
        Node node = composite.getDelegate();
        writer.writeStartElement("node");
        writer.writeAttribute("id", id(node));
        decorator.writeNodeAttributes(writer,rule, composite);
        writeLabels(writer, node);
        writeLabelsAsData(writer, node);
        decorator.writeNodeElements(writer, rule, composite);
        int props = writeProps(writer, node);
        if (withEnd)
            endElement(writer);

        return props;
    }

    private String id(Node node) {
        return "n" + node.getId();
    }

    private void writeLabels(XMLStreamWriter writer, Node node) throws IOException, XMLStreamException {
        String labelsString = getLabelsString(node);
        if (!labelsString.isEmpty())
            writer.writeAttribute("labels", labelsString);
    }

    private void writeLabelsAsData(XMLStreamWriter writer, Node node) throws IOException, XMLStreamException {
        String labelsString = getLabelsString(node);
        if (labelsString.isEmpty())
            return;
        writeData(writer, "labels", labelsString);
    }

    private int writeRelationship(XMLStreamWriter writer, CompositeObject coRel) throws IOException, XMLStreamException {
        Relationship rel = coRel.getDelegate();

        writer.writeStartElement("edge");
        writer.writeAttribute("id", id(rel));
        writer.writeAttribute("source", id(rel.getStartNode()));
        writer.writeAttribute("target", id(rel.getEndNode()));
        writer.writeAttribute("label", rel.getType().name());
        writeData(writer, "label", rel.getType().name());
        int props = writeProps(writer, rel);
        endElement(writer);
        return props;
    }

    private String id(Relationship rel) {
        return "e" + rel.getId();
    }

    private void endElement(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeEndElement();
        newLine(writer);
    }

    private int writeProps(XMLStreamWriter writer, PropertyContainer node) throws IOException, XMLStreamException {
        int count = 0;
        for (String prop : node.getPropertyKeys()) {
            Object value = node.getProperty(prop);
            writeData(writer, prop, value);
            count++;
        }
        return count;
    }

    private void writeData(XMLStreamWriter writer, String prop, Object value) throws IOException, XMLStreamException {
        writer.writeStartElement("data");
        writer.writeAttribute("key", prop);
        if (value != null)
            writer.writeCharacters(value.toString());
        writer.writeEndElement();
    }

    private void writeFooter(XMLStreamWriter writer) throws IOException, XMLStreamException {
        endElement(writer);
        writer.writeEndDocument();
    }

    private void writeHeader(XMLStreamWriter writer, GraphMLNamespaceContext context) throws IOException, XMLStreamException {
        writer.writeStartDocument("UTF-8", "1.0");
        newLine(writer);
        writer.writeStartElement("graphml");
        writer.writeNamespace("xmlns", "http://graphml.graphdrawing.org/xmlns");
        //writer.writeAttribute("xmlns", "http://graphml.graphdrawing.org/xmlns", "xsi", "http://www.w3.org/2001/XMLSchema-instance");

        for (Map.Entry<String, String> entry : context.getNamespaces().entrySet()) {
            writer.writeAttribute("xmlns", "http://graphml.graphdrawing.org/xmlns", entry.getKey(), entry.getValue());
        }

        //decorator.writeAdditionalNamespace(writer);
        writer.writeAttribute("xsi", "", "schemaLocation",
                "http://graphml.graphdrawing.org/xmlns http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd");
        newLine(writer);
    }

    private void newLine(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeCharacters("\n");
    }

}
