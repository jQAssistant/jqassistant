package com.buschmais.jqassistant.plugin.graphml.report.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import com.buschmais.jqassistant.core.shared.reflection.ClassHelper;
import com.buschmais.jqassistant.plugin.graphml.report.api.GraphMLDecorator;
import com.buschmais.jqassistant.plugin.graphml.report.decorator.YedGraphMLDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphMLReportPlugin.class);

    private static final String CONCEPT_PATTERN = "graphml.report.conceptPattern";
    private static final String DIRECTORY = "graphml.report.directory";
    private static final String GRAPHML_DECORATOR = "graphml.decorator";
    private static final String FILEEXTENSION_GRAPHML = ".graphml";

    private String conceptPattern = ".*\\.graphml$";
    private String directory = "jqassistant/report";
    private XmlGraphMLWriter xmlGraphMLWriter;

    private Rule currentRule;

    @Override
    public void initialize() throws ReportException {
    }

    @Override
    public void configure(Map<String, Object> properties) throws ReportException {
        this.conceptPattern = getProperty(properties, CONCEPT_PATTERN, conceptPattern);
        this.directory = getProperty(properties, DIRECTORY, directory);
        String graphMLDecoratorClass = getProperty(properties, GRAPHML_DECORATOR, YedGraphMLDecorator.class.getName());
        Class<GraphMLDecorator> type = new ClassHelper(GraphMLReportPlugin.class.getClassLoader()).getType(graphMLDecoratorClass);
        xmlGraphMLWriter = new XmlGraphMLWriter(type, properties);
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
        this.currentRule = concept;
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
        this.currentRule = constraint;
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
                if (directory.mkdirs()) {
                    LOGGER.info("Created directory " + directory.getAbsolutePath());
                }
                File file = new File(directory, fileName);
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
                xmlGraphMLWriter.write(result, subGraph, file);
            } catch (IOException | XMLStreamException e) {
                throw new ReportException("Cannot write custom report.", e);
            }
        }
    }
}
