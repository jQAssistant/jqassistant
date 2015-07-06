package com.buschmais.jqassistant.plugin.graphml.report.impl;

import static com.buschmais.jqassistant.plugin.graphml.report.impl.MetaInformation.getLabelsString;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

import com.buschmais.jqassistant.core.report.api.ReportHelper;
import com.buschmais.xo.api.CompositeObject;
import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

/**
 * @author mh
 * @since 21.01.14
 */
public class XmlGraphMLWriter {

    private XMLOutputFactory xmlOutputFactory;

    XmlGraphMLWriter() {
        xmlOutputFactory = XMLOutputFactory.newInstance();
    }

    void write(SimpleSubGraph graph, Writer writer) throws IOException, XMLStreamException {
        Collection<CompositeObject> allCoNodes = graph.getAllNodes();
        XMLStreamWriter xmlWriter = new IndentingXMLStreamWriter(xmlOutputFactory.createXMLStreamWriter(writer));
        NamespaceContext context = createNamespaceContext();
        if (context != null) {
            xmlWriter.setNamespaceContext(context);
        }
        writeHeader(xmlWriter);
        writeDefaultKeys(xmlWriter);
        writeKeyTypes(xmlWriter, graph);
        writeSubgraph(graph, xmlWriter);

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

    /**
     * Creates a {@link NamespaceContext} for the xml writer. Return
     * <code>null</code> to add no context.
     * 
     * @return a {@link NamespaceContext}
     */
    protected NamespaceContext createNamespaceContext() {
        return null;
    }

    private void writeSubgraph(SimpleSubGraph graph, XMLStreamWriter writer) throws XMLStreamException, IOException {
        CompositeObject wrapperNode = graph.getParentNode();
        if (wrapperNode != null) {
            writeNode(writer, wrapperNode, false);
        }

        writer.writeStartElement("graph");
        writer.writeAttribute("id", "G" + graph.hashCode());
        writer.writeAttribute("edgedefault", "directed");
        newLine(writer);

        for (CompositeObject node : graph.getNodes()) {
            writeNode(writer, node, true);
        }

        for (SimpleSubGraph subgraph : graph.getSubgraphs()) {
            writeSubgraph(subgraph, writer);
        }

        endElement(writer);

        if (wrapperNode != null) {
            writer.writeEndElement();
        }
    }

    /**
     * Writes a bunch of keys in the graphml-Tag that will be used for formating
     * or so. This method can be overwritten if any special default keys are
     * necessary. Please call super to ensure all needed keys will be created.
     * 
     * @param writer
     *            the XMLWriter
     * @throws XMLStreamException
     */
    protected void writeDefaultKeys(XMLStreamWriter writer) throws XMLStreamException {
        // no keys written in the moment
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

    private int writeNode(XMLStreamWriter writer, CompositeObject composite, boolean withEnd) throws IOException, XMLStreamException {
        Node node = composite.getDelegate();
        writer.writeStartElement("node");
        writer.writeAttribute("id", id(node));
        writeAdditionalNodeAttribute(writer, node);
        writeLabels(writer, node);
        writeLabelsAsData(writer, node);
        writeAdditionalNodeData(writer, ReportHelper.getStringValue(composite));
        int props = writeProps(writer, node);
        if (withEnd)
            endElement(writer);

        return props;
    }

    /**
     * Can be overwritten to add additional node attributes. Please call super
     * to ensure all necessary attributes will be written.
     * 
     * @param writer
     *            the xml writer
     * @param node
     *            the node
     * @throws XMLStreamException
     */
    protected void writeAdditionalNodeAttribute(XMLStreamWriter writer, Node node) throws XMLStreamException {
        // nothing todo here
    }

    /**
     * Used to insert additional elements inside a node-element. Can be
     * overwriten, but please call super to ensure all needed elements will be
     * created.
     * 
     * @param writer
     *            the xml writer
     * @param nodeLabel
     *            the label of the node
     * @throws XMLStreamException
     */
    protected void writeAdditionalNodeData(XMLStreamWriter writer, String nodeLabel) throws XMLStreamException {
        // nothing to do here
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

    private void writeHeader(XMLStreamWriter writer) throws IOException, XMLStreamException {
        writer.writeStartDocument("UTF-8", "1.0");
        newLine(writer);
        writer.writeStartElement("graphml");
        writer.writeNamespace("xmlns", "http://graphml.graphdrawing.org/xmlns");
        writer.writeAttribute("xmlns", "http://graphml.graphdrawing.org/xmlns", "xsi", "http://www.w3.org/2001/XMLSchema-instance");
        writeAdditionalNamespace(writer);
        writer.writeAttribute("xsi", "", "schemaLocation", "http://graphml.graphdrawing.org/xmlns http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd");
        newLine(writer);
    }

    /**
     * Can be used to add additional Namespace attriobutes to the graphml-root
     * element. If you overwrite these methode please call super to ensure all
     * needed namespaces will be added.
     * 
     * @param writer
     *            the XML Writer
     * @throws XMLStreamException
     */
    protected void writeAdditionalNamespace(XMLStreamWriter writer) throws XMLStreamException {
    }

    private void newLine(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeCharacters("\n");
    }

}
