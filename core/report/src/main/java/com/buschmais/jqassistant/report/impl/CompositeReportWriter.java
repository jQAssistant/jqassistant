package com.buschmais.jqassistant.report.impl;

import com.buschmais.jqassistant.core.model.api.Concept;
import com.buschmais.jqassistant.core.model.api.Constraint;
import com.buschmais.jqassistant.core.model.api.ConstraintGroup;
import com.buschmais.jqassistant.core.model.api.Result;
import com.buschmais.jqassistant.report.api.ReportWriter;
import com.buschmais.jqassistant.report.api.ReportWriterException;

/**
 * A {@link ReportWriter} implementation which  delegates all method calls to the {@link ReportWriter}s  provided by the {@link Iterable} constructor argument.
 */
public class CompositeReportWriter implements ReportWriter {

    private static interface DelegateOperation {
        void run(ReportWriter reportWriter) throws ReportWriterException;
    }

    private Iterable<ReportWriter> reportWriters;

    public CompositeReportWriter(Iterable<ReportWriter> reportWriters) {
        this.reportWriters = reportWriters;
    }

    @Override
    public void begin() throws ReportWriterException {
        run(new DelegateOperation() {
            @Override
            public void run(ReportWriter reportWriter) throws ReportWriterException {
                reportWriter.begin();
            }
        });
    }

    @Override
    public void end() throws ReportWriterException {
        run(new DelegateOperation() {
            @Override
            public void run(ReportWriter reportWriter) throws ReportWriterException {
                reportWriter.end();
            }
        });
    }

    @Override
    public void beginConcept(final Concept concept) throws ReportWriterException {
        run(new DelegateOperation() {
            @Override
            public void run(ReportWriter reportWriter) throws ReportWriterException {
                reportWriter.beginConcept(concept);
            }
        });
    }

    @Override
    public void endConcept() throws ReportWriterException {
        run(new DelegateOperation() {
            @Override
            public void run(ReportWriter reportWriter) throws ReportWriterException {
                reportWriter.endConcept();
            }
        });
    }

    @Override
    public void beginConstraintGroup(final ConstraintGroup constraintGroup) throws ReportWriterException {
        run(new DelegateOperation() {
            @Override
            public void run(ReportWriter reportWriter) throws ReportWriterException {
                reportWriter.beginConstraintGroup(constraintGroup);
            }
        });
    }

    @Override
    public void endConstraintGroup() throws ReportWriterException {
        run(new DelegateOperation() {
            @Override
            public void run(ReportWriter reportWriter) throws ReportWriterException {
                reportWriter.endConstraintGroup();
            }
        });
    }

    @Override
    public void beginConstraint(final Constraint constraint) throws ReportWriterException {
        run(new DelegateOperation() {
            @Override
            public void run(ReportWriter reportWriter) throws ReportWriterException {
                reportWriter.beginConstraint(constraint);
            }
        });
    }

    @Override
    public void endConstraint() throws ReportWriterException {
        run(new DelegateOperation() {
            @Override
            public void run(ReportWriter reportWriter) throws ReportWriterException {
                reportWriter.endConstraint();
            }
        });
    }

    @Override
    public void setResult(final Result result) throws ReportWriterException {
        run(new DelegateOperation() {
            @Override
            public void run(ReportWriter reportWriter) throws ReportWriterException {
                reportWriter.setResult(result);
            }
        });
    }

    private void run(DelegateOperation operation) throws ReportWriterException {
        for (ReportWriter reportWriter : reportWriters) {
            operation.run(reportWriter);
        }
    }
}
