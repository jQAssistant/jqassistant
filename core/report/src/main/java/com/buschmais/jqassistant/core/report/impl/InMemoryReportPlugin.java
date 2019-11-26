package com.buschmais.jqassistant.core.report.impl;

import java.util.Map;
import java.util.TreeMap;

import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.Group;
import com.buschmais.jqassistant.core.rule.api.model.Rule;

/**
 * A {@link ReportPlugin}
 * implementation collection the concept results and constraint violations
 * in-memory.
 */
public class InMemoryReportPlugin implements ReportPlugin {

    private ReportPlugin delegate;

    private Map<String, Result<Concept>> conceptResults = new TreeMap<>();

    private Map<String, Result<Constraint>> constraintResults = new TreeMap<>();
    private Result<? extends Rule> currentResult;

    public InMemoryReportPlugin(ReportPlugin delegate) {
        this.delegate = delegate;
    }

    @Override
    public void begin() throws ReportException {
        delegate.begin();
    }

    @Override
    public void end() throws ReportException {
        delegate.end();
    }

    @Override
    public void beginConcept(Concept concept) throws ReportException {
        delegate.beginConcept(concept);
    }

    @Override
    public void endConcept() throws ReportException {
        addResult(this.conceptResults);
        delegate.endConcept();
    }

    @Override
    public void beginGroup(Group group) throws ReportException {
        delegate.beginGroup(group);
    }

    @Override
    public void endGroup() throws ReportException {
        delegate.endGroup();
    }

    @Override
    public void beginConstraint(Constraint constraint) throws ReportException {
        delegate.beginConstraint(constraint);
    }

    @Override
    public void endConstraint() throws ReportException {
        addResult(this.constraintResults);
        delegate.endConstraint();
    }

    @Override
    public void setResult(Result<? extends ExecutableRule> result) throws ReportException {
        this.currentResult = result;
        delegate.setResult(result);
    }

    public Map<String, Result<Concept>> getConceptResults() {
        return this.conceptResults;
    }

    public Map<String, Result<Constraint>> getConstraintResults() {
        return this.constraintResults;
    }

    @SuppressWarnings("unchecked")
    private <T extends ExecutableRule> void addResult(Map<String, Result<T>> results) {
        if (currentResult != null) {
            results.put(currentResult.getRule().getId(), (Result<T>) currentResult);
            this.currentResult = null;
        }
    }
}
