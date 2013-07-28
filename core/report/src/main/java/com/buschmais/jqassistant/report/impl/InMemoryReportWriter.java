package com.buschmais.jqassistant.report.impl;

import com.buschmais.jqassistant.report.api.ReportWriter;
import com.buschmais.jqassistant.core.model.api.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link ReportWriter} implementation collection the concept results and constraint violations in-memory.
 */
public class InMemoryReportWriter implements ReportWriter {

    private List<Result<Concept>> conceptResults = new ArrayList<Result<Concept>>();

    private List<Result<Constraint>> constraintViolations = new ArrayList<Result<Constraint>>();


    private Result currentResult;

    @Override
    public void begin() {
    }

    @Override
    public void end() {
    }

    @Override
    public void beginConcept(Concept concept) {
    }

    @Override
    public void endConcept() {
        addResult(this.conceptResults);
    }

    @Override
    public void beginConstraintGroup(ConstraintGroup constraintGroup) {
    }

    @Override
    public void endConstraintGroup() {
    }

    @Override
    public void beginConstraint(Constraint constraint) {
    }

    @Override
    public void endConstraint() {
        addResult(this.constraintViolations);
    }

    @Override
    public void setResult(Result result) {
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
