package com.buschmais.jqassistant.report.impl;

import com.buschmais.jqassistant.core.model.api.Concept;
import com.buschmais.jqassistant.core.model.api.Constraint;
import com.buschmais.jqassistant.core.model.api.ConstraintGroup;
import com.buschmais.jqassistant.core.model.api.Result;
import com.buschmais.jqassistant.report.api.ReportWriter;

/**
 * A {@link ReportWriter} implementation which  delegates all method calls to the {@link ReportWriter}s  provided by the {@link Iterable} constructor argument.
 */
public class CompositeReportWriter implements ReportWriter {

    private static interface DelegateOperation {
        void run(ReportWriter reportWriter);
    }

    private Iterable<ReportWriter> reportWriters;

    public CompositeReportWriter(Iterable<ReportWriter> reportWriters) {
        this.reportWriters = reportWriters;
    }

    @Override
    public void begin() {
        run(new DelegateOperation() {
            @Override
            public void run(ReportWriter reportWriter) {
                reportWriter.begin();
            }
        });
    }

    @Override
    public void end() {
        run(new DelegateOperation() {
            @Override
            public void run(ReportWriter reportWriter) {
                reportWriter.end();
            }
        });
    }

    @Override
    public void beginConcept(final Concept concept) {
        run(new DelegateOperation() {
            @Override
            public void run(ReportWriter reportWriter) {
                reportWriter.beginConcept(concept);
            }
        });
    }

    @Override
    public void endConcept() {
        run(new DelegateOperation() {
            @Override
            public void run(ReportWriter reportWriter) {
                reportWriter.endConcept();
            }
        });
    }

    @Override
    public void beginConstraintGroup(final ConstraintGroup constraintGroup) {
        run(new DelegateOperation() {
            @Override
            public void run(ReportWriter reportWriter) {
                reportWriter.beginConstraintGroup(constraintGroup);
            }
        });
    }

    @Override
    public void endConstraintGroup() {
        run(new DelegateOperation() {
            @Override
            public void run(ReportWriter reportWriter) {
                reportWriter.endConstraint();
            }
        });
    }

    @Override
    public void beginConstraint(final Constraint constraint) {
        run(new DelegateOperation() {
            @Override
            public void run(ReportWriter reportWriter) {
                reportWriter.beginConstraint(constraint);
            }
        });
    }

    @Override
    public void endConstraint() {
        run(new DelegateOperation() {
            @Override
            public void run(ReportWriter reportWriter) {
                reportWriter.endConstraint();
            }
        });
    }

    @Override
    public void setResult(final Result result) {
        run(new DelegateOperation() {
            @Override
            public void run(ReportWriter reportWriter) {
                reportWriter.setResult(result);
            }
        });
    }

    private void run(DelegateOperation operation) {
        for (ReportWriter reportWriter : reportWriters) {
            operation.run(reportWriter);
        }
    }
}
