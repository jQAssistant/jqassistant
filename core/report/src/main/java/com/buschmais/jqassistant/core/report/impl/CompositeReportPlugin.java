package com.buschmais.jqassistant.core.report.impl;

import java.util.*;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;

/**
 * A {@link com.buschmais.jqassistant.core.report.api.ReportPlugin} implementation which delegates all method calls to the {@link ReportPlugin}s.
 * <p>
 * A rule (i.e. concept or concept) may explicitly select one or more reports by their id to delegate to.
 */
public class CompositeReportPlugin implements ReportPlugin {

    private interface DelegateOperation {
        void run(ReportPlugin reportWriter) throws ReportException;
    }

    private Map<String, ReportPlugin> reportWriters;

    private Iterable<ReportPlugin> selectedReportWriters = Collections.emptyList();

    public CompositeReportPlugin(Map<String, ReportPlugin> reportWriters) {
        this.reportWriters = reportWriters;
        this.selectedReportWriters = reportWriters.values();
    }

    @Override
    public void initialize() throws ReportException {
    }

    @Override
    public void configure(Map<String, Object> properties) throws ReportException {
    }

    @Override
    public void begin() throws ReportException {
        run(new DelegateOperation() {
            @Override
            public void run(ReportPlugin reportWriter) throws ReportException {
                reportWriter.begin();
            }
        });
    }

    @Override
    public void end() throws ReportException {
        run(new DelegateOperation() {
            @Override
            public void run(ReportPlugin reportWriter) throws ReportException {
                reportWriter.end();
            }
        });
    }

    @Override
    public void beginConcept(final Concept concept) throws ReportException {
        selectReportWriter(concept);
        run(new DelegateOperation() {
            @Override
            public void run(ReportPlugin reportWriter) throws ReportException {
                reportWriter.beginConcept(concept);
            }
        });
    }

    @Override
    public void endConcept() throws ReportException {
        run(new DelegateOperation() {
            @Override
            public void run(ReportPlugin reportWriter) throws ReportException {
                reportWriter.endConcept();
            }
        });
        resetReportWriter();
    }

    @Override
    public void beginGroup(final Group group) throws ReportException {
        run(new DelegateOperation() {
            @Override
            public void run(ReportPlugin reportWriter) throws ReportException {
                reportWriter.beginGroup(group);
            }
        });
    }

    @Override
    public void endGroup() throws ReportException {
        run(new DelegateOperation() {
            @Override
            public void run(ReportPlugin reportWriter) throws ReportException {
                reportWriter.endGroup();
            }
        });
    }

    @Override
    public void beginConstraint(final Constraint constraint) throws ReportException {
        selectReportWriter(constraint);
        run(new DelegateOperation() {
            @Override
            public void run(ReportPlugin reportWriter) throws ReportException {
                reportWriter.beginConstraint(constraint);
            }
        });
    }

    @Override
    public void endConstraint() throws ReportException {
        run(new DelegateOperation() {
            @Override
            public void run(ReportPlugin reportWriter) throws ReportException {
                reportWriter.endConstraint();
            }
        });
        resetReportWriter();
    }

    @Override
    public void setResult(final Result<? extends ExecutableRule> result) throws ReportException {
        run(new DelegateOperation() {
            @Override
            public void run(ReportPlugin reportWriter) throws ReportException {
                reportWriter.setResult(result);
            }
        });
    }

    private void run(DelegateOperation operation) throws ReportException {
        for (ReportPlugin reportWriter : selectedReportWriters) {
            operation.run(reportWriter);
        }
    }

    /**
     * Select the report writers for the given rule.
     *
     * @param rule The rule.
     * @throws ReportException If no writer exists for a specified id.
     */
    private void selectReportWriter(ExecutableRule rule) throws ReportException {
        Set<String> selection = rule.getReport().getSelectedTypes();
        if (selection == null) {
            // no writer explicitly selected, use all registered.
            selectedReportWriters = reportWriters.values();
        } else {
            List<ReportPlugin> reportPlugins = new ArrayList<>();
            for (String type : selection) {
                ReportPlugin reportPlugin = this.reportWriters.get(type);
                if (reportPlugin == null) {
                    throw new ReportException("Unknown report selection '" + type + "' selected for '" + rule + "'");
                }
                reportPlugins.add(reportPlugin);
            }
            this.selectedReportWriters = reportPlugins;
        }
    }

    private void resetReportWriter() {
        selectedReportWriters = reportWriters.values();
    }
}
