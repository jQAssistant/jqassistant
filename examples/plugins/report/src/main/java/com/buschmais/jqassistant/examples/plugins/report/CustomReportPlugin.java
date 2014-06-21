package com.buschmais.jqassistant.examples.plugins.report;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.analysis.api.rule.Rule;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

public class CustomReportPlugin implements ReportPlugin {

    private static final String PROPERTY_FILENAME = "customReport.fileName";

    private String fileName;

    @Override
    public void initialize(Map<String, Object> properties) throws ReportException {
        this.fileName = (String) properties.get(PROPERTY_FILENAME);
        if (this.fileName == null) {
            throw new ReportException("Property " + PROPERTY_FILENAME + " is not specified.");
        }
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
    public void setResult(Result<? extends Rule> result) throws ReportException {
        Rule rule = result.getRule();
        if (rule instanceof Concept && "example:MethodsPerType".equals(rule.getId())) {
            try {
                PrintWriter writer = new PrintWriter(new FileWriter(fileName));
                writer.println("Methods per Type");
                writer.println("================");
                for (Map<String, Object> row : result.getRows()) {
                    TypeDescriptor type = (TypeDescriptor) row.get("Type");
                    Long methodCount = (Long) row.get("MethodCount");
                    writer.println(type.getFullQualifiedName() + ":" + methodCount);
                }
                writer.close();
            } catch (IOException e) {
                throw new ReportException("Cannot write custom report.", e);
            }
        }
    }
}
