package com.buschmais.jqassistant.scm.maven.integration.plugin;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;

public class CustomReportPlugin implements ReportPlugin {

    private static final String PROPERTY_FILENAME = "customReport.fileName";

    private String fileName;

    @Override
    public void initialize() throws ReportException {
    }

    @Override
    public void configure(Map<String, Object> properties) throws ReportException {
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
    public void setResult(Result<? extends ExecutableRule> result) throws ReportException {
        try {
            Writer writer = new FileWriter(fileName);
            writer.write("CustomReport");
            writer.close();
        } catch (IOException e) {
            throw new ReportException("Cannot write custom report.", e);
        }
    }
}
