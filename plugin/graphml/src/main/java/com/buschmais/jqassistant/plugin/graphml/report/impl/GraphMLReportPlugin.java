package com.buschmais.jqassistant.plugin.graphml.report.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.neo4j.cypher.export.SubGraph;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.schema.ConstraintDefinition;
import org.neo4j.graphdb.schema.IndexDefinition;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.xo.api.CompositeObject;

/**
 * A report plugin that creates GraphML files based on the results of a concept.
 * 
 * @author mh
 * @author Dirk Mahler
 */
public class GraphMLReportPlugin implements ReportPlugin {

    private static final String CONCEPT_PATTERN = "graphml.report.conceptPattern";
    private static final String DIRECTORY = "graphml.report.directory";
    private static final String FILEEXTENSION_GRAPHML = ".graphml";

    private String conceptPattern = ".*\\.graphml$";
    private String directory = "jqassistant/report";

    private XmlGraphMLWriter xmlGraphMLWriter;

    @Override
    public void initialize() throws ReportException {
        xmlGraphMLWriter = new XmlGraphMLWriter();
    }

    @Override
    public void configure(Map<String, Object> properties) throws ReportException {
        this.conceptPattern = getProperty(properties, CONCEPT_PATTERN, conceptPattern);
        this.directory = getProperty(properties, DIRECTORY, directory);
    }

    private String getProperty(Map<String, Object> properties, String property, String defaultValue) throws ReportException {
        String value = (String) properties.get(property);
        return value != null ? value : defaultValue;
    }

    @Override
    public void begin() throws ReportException {
    }

    @Override
    public void end() throws ReportException {
    }

    @Override
    public void beginConcept(Concept concept) throws ReportException {
    }

    @Override
    public void endConcept() throws ReportException {
    }

    @Override
    public void beginGroup(Group group) throws ReportException {
    }

    @Override
    public void endGroup() throws ReportException {
    }

    @Override
    public void beginConstraint(Constraint constraint) throws ReportException {
    }

    @Override
    public void endConstraint() throws ReportException {
    }

    @Override
    public void setResult(Result<? extends ExecutableRule> result) throws ReportException {
        Rule rule = result.getRule();
        if (rule instanceof Concept && rule.getId().matches(conceptPattern)) {
            try {
                String fileName = rule.getId().replaceAll("\\:", "_");
                if (!fileName.endsWith(FILEEXTENSION_GRAPHML)) {
                    fileName = fileName + FILEEXTENSION_GRAPHML;
                }
                File directory = new File(this.directory);
                directory.mkdirs();
                File file = new File(directory, fileName);
                PrintWriter writer = new PrintWriter(new FileWriter(file));
                SimpleSubGraph subGraph = new SimpleSubGraph();
                for (Map<String, Object> row : result.getRows()) {
                    for (Object value : row.values()) {
                        if (value instanceof Map) {
                            Map m = (Map) value;
                            if (VirtualRelationship.isRelationship(m)) {
                                subGraph.add(new VirtualRelationship(m));
                            }
                        }
                        if (value instanceof CompositeObject) {
                            CompositeObject compositeObject = (CompositeObject) value;
                            Object delegate = compositeObject.getDelegate();
                            subGraph.add(delegate);
                        }
                    }
                }
                xmlGraphMLWriter.write(subGraph, writer);
                writer.close();
            } catch (IOException | XMLStreamException e) {
                throw new ReportException("Cannot write custom report.", e);
            }
        }
    }

    private static class SimpleSubGraph implements SubGraph {
        private Set<Node> nodes = new LinkedHashSet<>(1000);
        private Set<Relationship> relationships = new LinkedHashSet<>(1000);

        @Override
        public Iterable<Node> getNodes() {
            return nodes;
        }

        @Override
        public Iterable<Relationship> getRelationships() {
            return relationships;
        }

        @Override
        public boolean contains(Relationship relationship) {
            return relationships.contains(relationship);
        }

        @Override
        public Iterable<IndexDefinition> getIndexes() {
            return null;
        }

        @Override
        public Iterable<ConstraintDefinition> getConstraints() {
            return null;
        }

        public void add(Object value) {
            if (value instanceof Node) {
                nodes.add((Node) value);
            } else if (value instanceof Relationship) {
                relationships.add((Relationship) value);
            } else if (value instanceof Iterable) {
                for (Object o : (Iterable) value)
                    add(o);
            }
        }
    }

    static class VirtualRelationship implements Relationship {
        static long REL_ID = -1;

        private final long id;
        private final Node start;
        private final Node end;
        private final RelationshipType type;
        private final Map<String, Object> props = new LinkedHashMap<>();

        public static boolean isRelationship(Map m) {
            return m.containsKey("type") && m.containsKey("startNode") && m.containsKey("endNode");
        }

        public VirtualRelationship(Map m) {
            if (!isRelationship(m))
                throw new IllegalArgumentException("Not a relationship-map " + m);
            this.start = (Node) m.get("startNode");
            this.end = (Node) m.get("endNode");
            this.type = DynamicRelationshipType.withName((String) m.get("type"));
            this.id = m.containsKey("id") ? ((Number)m.get("id")).longValue() : REL_ID--;
            if (m.containsKey("properties")) {
               this.props.putAll((Map)m.get("properties"));
            }
        }

        @Override
        public long getId() {
            return id;
        }

        @Override
        public void delete() {
        }

        @Override
        public Node getStartNode() {
            return start;
        }

        @Override
        public Node getEndNode() {
            return end;
        }

        @Override
        public Node getOtherNode(Node node) {
            if (node.equals(start))
                return end;
            if (node.equals(end))
                return start;
            throw new IllegalArgumentException("Node is neither start nor end-node " + node);
        }

        @Override
        public Node[] getNodes() {
            return new Node[] { start, end };
        }

        @Override
        public RelationshipType getType() {
            return type;
        }

        @Override
        public boolean isType(RelationshipType type) {
            return type.name().equals(this.type.name());
        }

        @Override
        public GraphDatabaseService getGraphDatabase() {
            return null;
        }

        @Override
        public boolean hasProperty(String key) {
            return props.containsKey(key);
        }

        @Override
        public Object getProperty(String key) {
            return props.get(key);
        }

        @Override
        public Object getProperty(String key, Object defaultValue) {
            return props.containsKey(key) ? props.get(key) : defaultValue;
        }

        @Override
        public void setProperty(String key, Object value) {
            props.put(key, value);
        }

        @Override
        public Object removeProperty(String key) {
            return props.remove(key);
        }

        @Override
        public Iterable<String> getPropertyKeys() {
            return props.keySet();
        }
    }
}
