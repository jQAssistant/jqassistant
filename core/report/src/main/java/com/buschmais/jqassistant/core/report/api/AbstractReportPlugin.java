package com.buschmais.jqassistant.core.report.api;

import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;

public abstract class AbstractReportPlugin implements ReportPlugin {

    @Override
    public void initialize() throws ReportException {
    }

    @Override
    public void configure(ReportContext reportContext, Map<String, Object> properties) throws ReportException {
    }

    @Override
    public void begin() throws ReportException {
    }

    @Override
    public void end() throws ReportException {
    }

    @Override
    public void beginGroup(Group group) throws ReportException {
    }

    @Override
    public void endGroup() throws ReportException {
    }

    @Override
    public void beginConcept(Concept concept) throws ReportException {
    }

    @Override
    public void endConcept() throws ReportException {
    }

    @Override
    public void beginConstraint(Constraint constraint) throws ReportException {
    }

    @Override
    public void endConstraint() throws ReportException {
    }

    @Override
    public void setResult(Result<? extends ExecutableRule> result) throws ReportException {
    }

}
