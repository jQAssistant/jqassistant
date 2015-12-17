package com.buschmais.jqassistant.core.report.impl;

import java.util.Map;
import java.util.TreeMap;

import com.buschmais.jqassistant.core.analysis.api.AnalysisListener;
import com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.*;

/**
 * A {@link com.buschmais.jqassistant.core.analysis.api.AnalysisListener}
 * implementation collection the concept results and constraint violations
 * in-memory.
 */
public class InMemoryReportWriter implements AnalysisListener<AnalysisListenerException> {

    private Map<String, Result<Concept>> conceptResults = new TreeMap<>();

    private Map<String, Result<Constraint>> constraintResults = new TreeMap<>();

    private Result<? extends Rule> currentResult;

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
        addResult(this.constraintResults);
    }

    @Override
    public void setResult(Result<? extends ExecutableRule> result) throws AnalysisListenerException {
        this.currentResult = result;
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
