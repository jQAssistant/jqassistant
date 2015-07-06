package com.buschmais.jqassistant.plugin.graphml.report.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.BooleanUtils;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.analysis.api.rule.Rule;
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
    private static final String YED_GRAPHML = "graphml.report.yedgraphml";
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
        if (BooleanUtils.toBoolean(getProperty(properties, YED_GRAPHML, Boolean.TRUE.toString()))) {
            xmlGraphMLWriter = new YedXmlGraphMLWriter();
        }

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

                            if (VirtualNode.isNode(m)) {
                                subGraph.add(new VirtualNode(m));
                            }

                            if (SimpleSubGraph.isSubgraph(m)) {
                                subGraph.add(new SimpleSubGraph(m));
                            }

                        }
                        if (value instanceof CompositeObject) {
                            subGraph.add(value);
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
}
