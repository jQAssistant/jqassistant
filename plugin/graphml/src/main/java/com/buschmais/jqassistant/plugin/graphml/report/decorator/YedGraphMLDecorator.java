package com.buschmais.jqassistant.plugin.graphml.report.decorator;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.report.api.ReportHelper;
import com.buschmais.jqassistant.plugin.graphml.report.api.GraphMLDecorator;
import com.buschmais.jqassistant.plugin.graphml.report.api.SubGraph;
import com.buschmais.xo.api.CompositeObject;
import org.graphstream.algorithm.APSP;
import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSink;
import org.graphstream.stream.file.FileSinkDGS;
import org.neo4j.graphdb.Relationship;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * A GraphML decorator for yEd.
 */
public class YedGraphMLDecorator implements GraphMLDecorator {

    private static final String Y_NAMESPACE_URI = "http://www.yworks.com/xml/graphml";
    private static final String YED_NAMESPACE_URI = "http://www.yworks.com/xml/yed/3";

    private XMLStreamWriter writer;
    protected SingleGraph graph;

    @Override
    public void initialize(Result<?> result, SubGraph subGraph, XMLStreamWriter xmlWriter, File file, Map<String, Object> properties) {
        this.writer = xmlWriter;

        if((result.getRule().getReport() != null) && (result.getRule().getReport().getProperties() != null)) {
            Properties reportProperties = result.getRule().getReport().getProperties();
            if(reportProperties.containsKey("layout")) {
                // Create a new graph mirroring the subGraph.
                graph = new SingleGraph("layout helper");
                Set<String> nodeIds = new HashSet<>();
                for (CompositeObject node : subGraph.getNodes()) {
                    if(!filterNode(node)) {
                        String nodeId = node.getId().toString();
                        if (!nodeIds.contains(nodeId)) {
                            graph.addNode(nodeId);
                            nodeIds.add(nodeId);
                        }
                    }
                }

                Set<String> edgeIds = new HashSet<>();
                for (CompositeObject edge : subGraph.getRelationships()) {
                    if(!filterEdge(edge)) {
                        String edgeId = edge.getId().toString();
                        String startId = Long.toString(((Relationship) edge.getDelegate()).getStartNode().getId());
                        String endId = Long.toString(((Relationship) edge.getDelegate()).getEndNode().getId());
                        if (nodeIds.contains(startId) && nodeIds.contains(endId) && !edgeIds.contains(edgeId)) {
                            graph.addEdge(edge.getId().toString(), startId, endId, true);
                            edgeIds.add(edgeId);
                        }
                    }
                }

                // Dump the graph so we can experiment with it.
                /*FileSink fs = new FileSinkDGS();
                try {
                    fs.writeAll(graph, "graph-data.dgs");
                } catch (IOException e) {
                    e.printStackTrace();
                }*/

                // Filter out any redundant edges
                // (direct edges, that can be replaced by a sequence of other edges)
                if((reportProperties.get("strip-redundant-edges") != null) &&
                        "true".equalsIgnoreCase(reportProperties.get("strip-redundant-edges").toString())) {
                    // Filter out redundant edges.
                    for(Edge edge : graph.getEdgeSet()) {
                        edge.addAttribute("weight", 1);
                    }
                    APSP apsp = new APSP();
                    apsp.init(graph);
                    apsp.setDirected(true);
                    apsp.setWeightAttributeName("weight");
                    apsp.compute();

                    Set<Edge> redundantEdges = new HashSet<>();
                    for(Node source : graph.getNodeSet()) {
                        // Remove any remaining redundant edges
                        for (Edge leavingEdge : source.getLeavingEdgeSet()) {
                            if (leavingEdge != null) {
                                Node target = leavingEdge.getTargetNode();
                                // For every other outgoing edge, check if we can reach target from that node.
                                for (Edge otherLeavingEdge : source.getLeavingEdgeSet()) {
                                    if ((otherLeavingEdge != null) && (otherLeavingEdge != leavingEdge)) {
                                        Node otherTarget = otherLeavingEdge.getTargetNode();
                                        APSP.APSPInfo info = otherTarget.getAttribute(APSP.APSPInfo.ATTRIBUTE_NAME);
                                        // if there is a path from otherTarget to target, then
                                        // the path from source to target is obsolete.
                                        double lengthTo = info.getLengthTo(target.getId());
                                        if (lengthTo != -1) {
                                            redundantEdges.add(leavingEdge);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    for(Edge redundantEdge : redundantEdges) {
                        graph.removeEdge(redundantEdge);
                    }
                }

                if("hierarchical".equals(reportProperties.getProperty("layout"))) {
                    // Apply hierarchical layout to the mirror graph.
                    Toolkit.computeLayout(graph, new HierarchicalLayout(600, 600), 0.5);
                }
            }
        }
    }

    protected boolean filterNode(CompositeObject node) {
        return false;
    }

    protected boolean filterEdge(CompositeObject edge) {
        return false;
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
    public void writeNodeAttributes(CompositeObject node) throws XMLStreamException {
        if(graph != null) {
            Node graphNode = graph.getNode(node.getId().toString());
            if (graphNode != null) {
                writer.writeAttribute("yfiles.foldertype", "folder");
            }
        }
    }

    @Override
    public void writeNodeElements(CompositeObject node) throws XMLStreamException {
        if(graph != null) {
            Node graphNode = graph.getNode(node.getId().toString());
            if (graphNode != null) {
                // Get the node from the mirror graph in order to get it's position.
                Geometry geometry = null;
                if (graphNode.hasAttribute("xyz")) {
                    double[] xyz = graphNode.getAttribute("xyz");
                    geometry = new Geometry();
                    geometry.x = (float) (xyz[0]);
                    geometry.y = (float) (xyz[1]);
                    geometry.width = 80;
                    geometry.height = 100;
                }

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
                writeGroupNodeElement(writer, nodeLabel, false, geometry, borderInsets);
                writeGroupNodeElement(writer, nodeLabel, true, geometry, new Insets());

                writer.writeEndElement();
                writer.writeEndElement();
                writer.writeEndElement();
            }
        }
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

    private void writeGroupNodeElement(XMLStreamWriter writer, String nodeLabel, boolean closed,
                                       Geometry geometry, Insets borderInsets) throws XMLStreamException {
        writer.writeStartElement(Y_NAMESPACE_URI, "GroupNode");

        if(geometry != null) {
            writer.writeEmptyElement(Y_NAMESPACE_URI, "Geometry");
            writer.writeAttribute("x", Float.toString(geometry.x));
            writer.writeAttribute("y", Float.toString(geometry.y));
            writer.writeAttribute("width", Float.toString(geometry.width));
            writer.writeAttribute("height", Float.toString(geometry.height));
        }

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

    private class Geometry {
        private float x;
        private float y;

        private float width;
        private float height;
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
