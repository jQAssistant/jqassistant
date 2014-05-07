package com.buschmais.jqassistant.core.report.impl;

import java.util.ArrayList;
import java.util.List;

import com.buschmais.jqassistant.core.analysis.api.AnalysisListener;
import com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.AbstractRule;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;

/**
 * A {@link com.buschmais.jqassistant.core.analysis.api.AnalysisListener}
 * implementation collection the concept results and constraint violations
 * in-memory.
 */
public class InMemoryReportWriter implements AnalysisListener {

    private List<Result<Concept>> conceptResults = new ArrayList<Result<Concept>>();

    private List<Result<Constraint>> constraintViolations = new ArrayList<Result<Constraint>>();

    private Result currentResult;

    @Override
    public void begin() throws AnalysisListenerException {
    }

    @Override
    public void end() throws AnalysisListenerException {
    }

    @Override
    public void beginConcept(Concept concept) throws AnalysisListenerException {
    }

    @Override
    public void endConcept() throws AnalysisListenerException {
        addResult(this.conceptResults);
    }

    @Override
    public void beginGroup(Group group) throws AnalysisListenerException {
    }

    @Override
    public void endGroup() throws AnalysisListenerException {
    }

    @Override
    public void beginConstraint(Constraint constraint) throws AnalysisListenerException {
    }

    @Override
    public void endConstraint() throws AnalysisListenerException {
        addResult(this.constraintViolations);
    }

    @Override
    public void setResult(Result result) throws AnalysisListenerException {
        this.currentResult = result;
    }

    public List<Result<Concept>> getConceptResults() {
        return this.conceptResults;
    }

    public List<Result<Constraint>> getConstraintViolations() {
        return this.constraintViolations;
    }

    private <T extends AbstractRule> void addResult(List<Result<T>> results) {
        if (currentResult != null) {
            results.add(currentResult);
            this.currentResult = null;
        }
    }
}
