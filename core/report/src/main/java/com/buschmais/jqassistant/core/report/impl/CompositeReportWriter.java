package com.buschmais.jqassistant.core.report.impl;

import com.buschmais.jqassistant.core.analysis.api.AnalysisListener;
import com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;

import java.util.*;

/**
 * A {@link AnalysisListener} implementation which delegates all method calls to the {@link AnalysisListener}s.
 * <p>
 * A rule (i.e. concept or concept) may explicitly select one or more reports by their id to delegate to.
 */
public class CompositeReportWriter implements AnalysisListener<AnalysisListenerException> {

    private interface DelegateOperation {
        void run(AnalysisListener reportWriter) throws AnalysisListenerException;
    }

    private Map<String, AnalysisListener> reportWriters;

    private Iterable<AnalysisListener> selectedReportWriters = Collections.emptyList();

    public CompositeReportWriter(Map<String, AnalysisListener> reportWriters) {
        this.reportWriters = reportWriters;
        this.selectedReportWriters = reportWriters.values();
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
        selectReportWriter(concept);
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
        resetReportWriter();
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
        selectReportWriter(constraint);
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
        resetReportWriter();
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
        for (AnalysisListener reportWriter : selectedReportWriters) {
            operation.run(reportWriter);
        }
    }

    /**
     * Select the report writers for the given rule.
     *
     * @param rule The rule.
     * @throws AnalysisListenerException If no writer exists for a specified id.
     */
    private void selectReportWriter(ExecutableRule rule) throws AnalysisListenerException {
        Set<String> selection = rule.getReport().getSelectedTypes();
        if (selection == null) {
            // no writer explicitly selected, use all registered.
            selectedReportWriters = reportWriters.values();
        } else {
            List<AnalysisListener> selectedReportWriters = new ArrayList<>();
            for (String type : selection) {
                AnalysisListener analysisListener = this.reportWriters.get(type);
                if (analysisListener == null) {
                    throw new AnalysisListenerException("Unknown report selection '" + type + "' selected for '" + rule + "'");
                }
                selectedReportWriters.add(analysisListener);
            }
            this.selectedReportWriters = selectedReportWriters;
        }
    }

    private void resetReportWriter() {
        selectedReportWriters = reportWriters.values();
    }
}
