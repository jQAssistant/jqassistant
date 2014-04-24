package com.buschmais.jqassistant.core.report.impl;

import java.util.ArrayList;
import java.util.List;

import com.buschmais.jqassistant.core.analysis.api.ExecutionListener;
import com.buschmais.jqassistant.core.analysis.api.ExecutionListenerException;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.AbstractExecutable;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;

/**
 * A {@link com.buschmais.jqassistant.core.analysis.api.ExecutionListener}
 * implementation collection the concept results and constraint violations
 * in-memory.
 */
public class InMemoryReportWriter implements ExecutionListener {

    private List<Result<Concept>> conceptResults = new ArrayList<Result<Concept>>();

    private List<Result<Constraint>> constraintViolations = new ArrayList<Result<Constraint>>();

    private Result currentResult;

    @Override
    public void begin() throws ExecutionListenerException {
    }

    @Override
    public void end() throws ExecutionListenerException {
    }

    @Override
    public void beginConcept(Concept concept) throws ExecutionListenerException {
    }

    @Override
    public void endConcept() throws ExecutionListenerException {
        addResult(this.conceptResults);
    }

    @Override
    public void beginGroup(Group group) throws ExecutionListenerException {
    }

    @Override
    public void endGroup() throws ExecutionListenerException {
    }

    @Override
    public void beginConstraint(Constraint constraint) throws ExecutionListenerException {
    }

    @Override
    public void endConstraint() throws ExecutionListenerException {
        addResult(this.constraintViolations);
    }

    @Override
    public void setResult(Result result) throws ExecutionListenerException {
        this.currentResult = result;
    }

    public List<Result<Concept>> getConceptResults() {
        return this.conceptResults;
    }

    public List<Result<Constraint>> getConstraintViolations() {
        return this.constraintViolations;
    }

    private <T extends AbstractExecutable> void addResult(List<Result<T>> results) {
        if (currentResult != null) {
            results.add(currentResult);
            this.currentResult = null;
        }
    }
}
