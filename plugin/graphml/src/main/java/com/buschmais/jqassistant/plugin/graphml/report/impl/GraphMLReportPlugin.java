package com.buschmais.jqassistant.plugin.graphml.report.impl;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.shared.reflection.ClassHelper;
import com.buschmais.jqassistant.plugin.graphml.report.api.GraphMLDecorator;
import com.buschmais.jqassistant.plugin.graphml.report.api.SubGraph;
import com.buschmais.jqassistant.plugin.graphml.report.decorator.YedGraphMLDecorator;
import com.buschmais.xo.api.CompositeObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * A report plugin that creates GraphML files based on the results of a concept.
 *
 * @author mh
 * @author Dirk Mahler
 */
public class GraphMLReportPlugin implements ReportPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphMLReportPlugin.class);

    public static final String GRAPHML = "graphml";

    public static final String FILEEXTENSION_GRAPHML = ".graphml";

    private static final String CONCEPT_PATTERN = "graphml.report.conceptPattern";
    private static final String DIRECTORY = "graphml.report.directory";
    private static final String GRAPHML_DEFAULT_DECORATOR = "graphml.report.defaultDecorator";

    private String conceptPattern = ".*\\.graphml$";
    private String directory = "jqassistant/report";
    private XmlGraphMLWriter xmlGraphMLWriter;

    @Override
    public void initialize() throws ReportException {
    }

    @Override
    public void configure(Map<String, Object> properties) throws ReportException {
        this.conceptPattern = getProperty(properties, CONCEPT_PATTERN, conceptPattern);
        this.directory = getProperty(properties, DIRECTORY, directory);
        String defaultDecorator = getProperty(properties, GRAPHML_DEFAULT_DECORATOR, YedGraphMLDecorator.class.getName());
        ClassHelper classHelper = new ClassHelper(GraphMLReportPlugin.class.getClassLoader());
        Class<GraphMLDecorator> defaultDecoratorType = classHelper.getType(defaultDecorator);
        xmlGraphMLWriter = new XmlGraphMLWriter(classHelper, defaultDecoratorType, properties);
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
        Set<String> selectedReports = result.getRule().getReport().getSelectedTypes();
        if ((selectedReports != null && selectedReports.contains(GRAPHML))
                || (rule instanceof Concept && rule.getId().matches(conceptPattern))) {
            SubGraph subGraph = getSubGraph(result);
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
                xmlGraphMLWriter.write(result, subGraph, file);
            } catch (IOException | XMLStreamException e) {
                throw new ReportException("Cannot write custom report.", e);
            }
        }
    }

    private SubGraph getSubGraph(Result<? extends ExecutableRule> result) {
        SubGraphImpl subGraph = new SubGraphImpl();
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
                    if (SubGraphImpl.isSubgraph(m)) {
                        subGraph.add(new SubGraphImpl(m));
                    }
                }
                if (value instanceof CompositeObject) {
                    subGraph.add(value);
                }
            }
        }
        return subGraph;
    }
}
