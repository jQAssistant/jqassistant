package com.buschmais.jqassistant.plugin.graphml.report.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.neo4j.graphdb.Node;

public class YedXmlGraphMLWriter extends XmlGraphMLWriter {

    private static final String Y_NAMESPACE_URI = "http://www.yworks.com/xml/graphml";

    @Override
    protected void writeDefaultKeys(XMLStreamWriter writer) throws XMLStreamException {
        super.writeDefaultKeys(writer);

        writer.writeEmptyElement("key");
        writer.writeAttribute("for", "graphml");
        writer.writeAttribute("id", "d0");
        writer.writeAttribute("yfiles.type", "resources");

        writer.writeEmptyElement("key");
        writer.writeAttribute("for", "port");
        writer.writeAttribute("id", "d1");
        writer.writeAttribute("yfiles.type", "portgraphics");

        writer.writeEmptyElement("key");
        writer.writeAttribute("for", "port");
        writer.writeAttribute("id", "d2");
        writer.writeAttribute("yfiles.type", "portgeometry");

        writer.writeEmptyElement("key");
        writer.writeAttribute("for", "port");
        writer.writeAttribute("id", "d3");
        writer.writeAttribute("yfiles.type", "portuserdata");

        writer.writeEmptyElement("key");
        writer.writeAttribute("for", "node");
        writer.writeAttribute("id", "d6");
        writer.writeAttribute("yfiles.type", "nodegraphics");

        writer.writeEmptyElement("key");
        writer.writeAttribute("for", "edge");
        writer.writeAttribute("id", "d10");
        writer.writeAttribute("yfiles.type", "edgegraphics");
    }

    @Override
    protected void writeAdditionalNodeAttribute(XMLStreamWriter writer, Node node) throws XMLStreamException {
        super.writeAdditionalNodeAttribute(writer, node);
        writer.writeAttribute("yfiles.foldertype", "folder");
    }

    @Override
    protected void writeAdditionalNodeData(XMLStreamWriter writer, String nodeLabel) throws XMLStreamException {
        super.writeAdditionalNodeData(writer, nodeLabel);

        writer.writeStartElement("data");
        writer.writeAttribute("key", "d6");
        writer.writeStartElement(Y_NAMESPACE_URI, "ProxyAutoBoundsNode");
        writer.writeStartElement(Y_NAMESPACE_URI, "Realizers");
        writer.writeAttribute("active", "1");
        Insets borderInsets = new Insets();
        borderInsets.bottom = 14;
        borderInsets.top = 5;
        borderInsets.left = 51;
        borderInsets.right = 49;

        borderInsets.bottomF = 14.0F;
        borderInsets.topF = 4.7470703125F;
        borderInsets.leftF = 50.5F;
        borderInsets.rightF = 48.9443359375F;

        writeGroupNodeElement(writer, nodeLabel, false, borderInsets);
        writeGroupNodeElement(writer, nodeLabel, true, new Insets());

        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
    }

    @Override
    protected void writeAdditionalNamespace(XMLStreamWriter writer) throws XMLStreamException {
        super.writeAdditionalNamespace(writer);
        writer.writeAttribute("xmlns", "http://graphml.graphdrawing.org/xmlns", "y", Y_NAMESPACE_URI);
        writer.writeAttribute("xmlns", "http://graphml.graphdrawing.org/xmlns", "yed", "http://www.yworks.com/xml/yed/3");
    }

    private void writeGroupNodeElement(XMLStreamWriter writer, String nodeLabel, boolean closed, Insets borderInsets) throws XMLStreamException {
        writer.writeStartElement(Y_NAMESPACE_URI, "GroupNode");

        writer.writeEmptyElement(Y_NAMESPACE_URI, "Fill");
        writer.writeAttribute("color", "#FFFFFF");
        writer.writeAttribute("transparent", "false");

        writer.writeEmptyElement(Y_NAMESPACE_URI, "BorderStyle");
        writer.writeAttribute("color", "#000000");
        writer.writeAttribute("type", "line");
        writer.writeAttribute("width", "1.0");

        writer.writeStartElement(Y_NAMESPACE_URI, "NodeLabel");
        writer.writeAttribute("alignment", "right");
        writer.writeAttribute("autoSizePolicy", "node_width");
        writer.writeAttribute("backgroundColor", "#FFFFFF");
        writer.writeAttribute("borderDistance", "0.0");
        writer.writeAttribute("fontFamily", "Dialog");
        writer.writeAttribute("fontSize", "15");
        writer.writeAttribute("fontStyle", "plain");
        writer.writeAttribute("hasLineColor", "false");
        writer.writeAttribute("modelName", "internal");
        writer.writeAttribute("modelPosition", "t");
        writer.writeAttribute("textColor", "#000000");
        writer.writeAttribute("visible", "true");
        writer.writeAttribute("x", "0.0");
        writer.writeAttribute("y", "0.0");
        writer.writeCData(nodeLabel);
        writer.writeEndElement();

        writer.writeEmptyElement(Y_NAMESPACE_URI, "Shape");
        writer.writeAttribute("type", "rectangle");

        writer.writeEmptyElement(Y_NAMESPACE_URI, "DropShadow");
        writer.writeAttribute("color", "#D2D2D2");
        writer.writeAttribute("offsetX", "4");
        writer.writeAttribute("offsetY", "4");

        writer.writeEmptyElement(Y_NAMESPACE_URI, "State");
        writer.writeAttribute("closed", Boolean.toString(closed));
        writer.writeAttribute("innerGraphDisplayEnabled", "false");

        writer.writeEmptyElement(Y_NAMESPACE_URI, "Insets");
        writer.writeAttribute("bottom", "15");
        writer.writeAttribute("bottomF", "15.0");
        writer.writeAttribute("left", "15");
        writer.writeAttribute("leftF", "15.0");
        writer.writeAttribute("right", "15");
        writer.writeAttribute("rightF", "15.0");
        writer.writeAttribute("top", "15");
        writer.writeAttribute("topF", "15.0");

        writer.writeEmptyElement(Y_NAMESPACE_URI, "BorderInsets");
        writer.writeAttribute("bottom", Integer.toString(borderInsets.bottom));
        writer.writeAttribute("bottomF", Float.toString(borderInsets.bottomF));
        writer.writeAttribute("left", Integer.toString(borderInsets.left));
        writer.writeAttribute("leftF", Float.toString(borderInsets.leftF));
        writer.writeAttribute("right", Integer.toString(borderInsets.right));
        writer.writeAttribute("rightF", Float.toString(borderInsets.rightF));
        writer.writeAttribute("top", Integer.toString(borderInsets.top));
        writer.writeAttribute("topF", Float.toString(borderInsets.topF));

        writer.writeEndElement();
    }

    private class Insets {
        private int bottom = 0;
        private int left = 0;
        private int right = 0;
        private int top = 0;

        private float bottomF = 0.0F;
        private float leftF = 0.0F;
        private float rightF = 0.0F;
        private float topF = 0.0F;
    }

    @Override
    protected NamespaceContext createNamespaceContext() {
        return new GraphMlNamespaceContext();
    }

    private class GraphMlNamespaceContext implements NamespaceContext {

        @Override
        public String getNamespaceURI(String prefix) {
            switch (prefix) {
            case XMLConstants.XMLNS_ATTRIBUTE:
                return "http://graphml.graphdrawing.org/xmlns";
            case "xsi":
                return XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;
            case "y":
                return Y_NAMESPACE_URI;
            case "yed":
                return "http://www.yworks.com/xml/yed/3";
            }

            return XMLConstants.DEFAULT_NS_PREFIX;
        }

        @Override
        public String getPrefix(String namespaceURI) {
            switch (namespaceURI) {
            case "http://graphml.graphdrawing.org/xmlns":
                return XMLConstants.XMLNS_ATTRIBUTE;
            case XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI:
                return "xsi";
            case Y_NAMESPACE_URI:
                return "y";
            case "http://www.yworks.com/xml/yed/3":
                return "yed";
            }

            return XMLConstants.DEFAULT_NS_PREFIX;
        }

        @Override
        public Iterator<String> getPrefixes(String namespaceURI) {
            return Collections.singletonList(getPrefix(namespaceURI)).iterator();
        }

    }
}
