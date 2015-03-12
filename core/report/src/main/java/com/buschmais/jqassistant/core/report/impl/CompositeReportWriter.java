package com.buschmais.jqassistant.core.report.impl;

import com.buschmais.jqassistant.core.analysis.api.AnalysisListener;
import com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;

/**
 * A {@link com.buschmais.jqassistant.core.analysis.api.AnalysisListener}
 * implementation which delegates all method calls to the
 * {@link com.buschmais.jqassistant.core.analysis.api.AnalysisListener}s
 * provided by the {@link Iterable} constructor argument.
 */
public class CompositeReportWriter implements AnalysisListener<AnalysisListenerException> {

    private static interface DelegateOperation {
        void run(AnalysisListener reportWriter) throws AnalysisListenerException;
    }

    private Iterable<AnalysisListener> reportWriters;

    public CompositeReportWriter(Iterable<AnalysisListener> reportWriters) {
        this.reportWriters = reportWriters;
    }

    @Override
    public void begin() throws AnalysisListenerException {
        run(new DelegateOperation() {
            @Override
            public void run(AnalysisListener reportWriter) throws AnalysisListenerException {
                reportWriter.begin();
            }
        });
    }

    @Override
    public void end() throws AnalysisListenerException {
        run(new DelegateOperation() {
            @Override
            public void run(AnalysisListener reportWriter) throws AnalysisListenerException {
                reportWriter.end();
            }
        });
    }

    @Override
    public void beginConcept(final Concept concept) throws AnalysisListenerException {
        run(new DelegateOperation() {
            @Override
            public void run(AnalysisListener reportWriter) throws AnalysisListenerException {
                reportWriter.beginConcept(concept);
            }
        });
    }

    @Override
    public void endConcept() throws AnalysisListenerException {
        run(new DelegateOperation() {
            @Override
            public void run(AnalysisListener reportWriter) throws AnalysisListenerException {
                reportWriter.endConcept();
            }
        });
    }

    @Override
    public void beginGroup(final Group group) throws AnalysisListenerException {
        run(new DelegateOperation() {
            @Override
            public void run(AnalysisListener reportWriter) throws AnalysisListenerException {
                reportWriter.beginGroup(group);
            }
        });
    }

    @Override
    public void endGroup() throws AnalysisListenerException {
        run(new DelegateOperation() {
            @Override
            public void run(AnalysisListener reportWriter) throws AnalysisListenerException {
                reportWriter.endGroup();
            }
        });
    }

    @Override
    public void beginConstraint(final Constraint constraint) throws AnalysisListenerException {
        run(new DelegateOperation() {
            @Override
            public void run(AnalysisListener reportWriter) throws AnalysisListenerException {
                reportWriter.beginConstraint(constraint);
            }
        });
    }

    @Override
    public void endConstraint() throws AnalysisListenerException {
        run(new DelegateOperation() {
            @Override
            public void run(AnalysisListener reportWriter) throws AnalysisListenerException {
                reportWriter.endConstraint();
            }
        });
    }

    @Override
    public void setResult(final Result<? extends ExecutableRule> result) throws AnalysisListenerException {
        run(new DelegateOperation() {
            @Override
            public void run(AnalysisListener reportWriter) throws AnalysisListenerException {
                reportWriter.setResult(result);
            }
        });
    }

    private void run(DelegateOperation operation) throws AnalysisListenerException {
        for (AnalysisListener reportWriter : reportWriters) {
            operation.run(reportWriter);
        }
    }
}
