package com.buschmais.jqassistant.report.impl;

import com.buschmais.jqassistant.core.model.api.*;
import com.buschmais.jqassistant.report.api.ReportWriter;
import com.buschmais.jqassistant.report.api.ReportWriterException;

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
    public void begin() throws ReportWriterException {
    }

    @Override
    public void end() throws ReportWriterException {
    }

    @Override
    public void beginConcept(Concept concept) throws ReportWriterException {
    }

    @Override
    public void endConcept() throws ReportWriterException {
        addResult(this.conceptResults);
    }

    @Override
    public void beginConstraintGroup(ConstraintGroup constraintGroup) throws ReportWriterException {
    }

    @Override
    public void endConstraintGroup() throws ReportWriterException {
    }

    @Override
    public void beginConstraint(Constraint constraint) throws ReportWriterException {
    }

    @Override
    public void endConstraint() throws ReportWriterException {
        addResult(this.constraintViolations);
    }

    @Override
    public void setResult(Result result) throws ReportWriterException {
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
