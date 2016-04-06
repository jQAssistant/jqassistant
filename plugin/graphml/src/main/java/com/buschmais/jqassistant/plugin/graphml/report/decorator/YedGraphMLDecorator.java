package com.buschmais.jqassistant.plugin.graphml.report.decorator;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.report.api.ReportHelper;
import com.buschmais.jqassistant.plugin.graphml.report.api.GraphMLDecorator;
import com.buschmais.jqassistant.core.store.api.model.SubGraph;
import com.buschmais.xo.api.CompositeObject;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * A GraphML decorator for yEd.
 */
public class YedGraphMLDecorator implements GraphMLDecorator {

    private static final String Y_NAMESPACE_URI = "http://www.yworks.com/xml/graphml";
    private static final String YED_NAMESPACE_URI = "http://www.yworks.com/xml/yed/3";

    private XMLStreamWriter writer;

    @Override
    public void initialize(Result<?> result, SubGraph subGraph, XMLStreamWriter xmlWriter, File file, Map<String, Object> properties) {
        this.writer = xmlWriter;
    }

    @Override
    public Map<String, String> getNamespaces() {
        Map<String, String> namespaces = new HashMap<>();
        namespaces.put("y", Y_NAMESPACE_URI);
        namespaces.put("yed", YED_NAMESPACE_URI);
        return namespaces;
    }

    @Override
    public Map<String, String> getSchemaLocations() {
        Map<String, String> schemaLocations = new HashMap<>();
        schemaLocations.put("http://graphml.graphdrawing.org/xmlns", "http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd");
        return schemaLocations;
    }

    @Override
    public void writeKeys() throws XMLStreamException {
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
    public boolean isWriteNode(CompositeObject node) {
        return true;
    }

    @Override
    public void writeNodeAttributes(CompositeObject node) throws XMLStreamException {
        writer.writeAttribute("yfiles.foldertype", "folder");
    }

    @Override
    public void writeNodeElements(CompositeObject node) throws XMLStreamException {
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

        String nodeLabel = ReportHelper.getLabel(node);
        writeGroupNodeElement(writer, nodeLabel, false, borderInsets);
        writeGroupNodeElement(writer, nodeLabel, true, new Insets());

        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
    }

    @Override
    public boolean isWriteRelationship(CompositeObject relationship) {
        return true;
    }

    @Override
    public void writeRelationshipAttributes(CompositeObject relationship) throws XMLStreamException {
    }

    @Override
    public void writeRelationshipElements(CompositeObject relationship) throws XMLStreamException {
    }

    @Override
    public void close() {
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

}
