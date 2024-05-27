package com.buschmais.jqassistant.core.report.impl;

import java.util.*;

import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.Group;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link com.buschmais.jqassistant.core.report.api.ReportPlugin}
 * implementation which delegates all method calls to the {@link ReportPlugin}s.
 * <p>
 * A rule (i.e. concept or concept) may explicitly select one or more reports by
 * their id to delegate to.
 */
public class CompositeReportPlugin implements ReportPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompositeReportPlugin.class);

    private interface ReportOperation {
        void run(ReportPlugin reportPlugin) throws ReportException;
    }

    private Map<String, ReportPlugin> selectableReportPlugins = new HashMap<>();

    private Map<String, ReportPlugin> defaultReportPlugins = new HashMap<>();

    private Map<String, ReportPlugin> selectedReportPlugins = Collections.emptyMap();

    /**
     * Constructor.
     *
     * @param reportPlugins
     *     The available {@link ReportPlugin}s.
     */
    public CompositeReportPlugin(Map<String, ReportPlugin> reportPlugins) {
        for (Map.Entry<String, ReportPlugin> entry : reportPlugins.entrySet()) {
            String id = entry.getKey();
            ReportPlugin reportPlugin = entry.getValue();
            if (reportPlugin.getClass()
                .isAnnotationPresent(Default.class)) {
                defaultReportPlugins.put(id, reportPlugin);
            }
            selectableReportPlugins.put(id, reportPlugin);
        }
        LOGGER.debug("Using {} as default reports.", defaultReportPlugins);
    }

    @Override
    public void begin() throws ReportException {
        this.selectedReportPlugins = selectableReportPlugins;
        run(ReportPlugin::begin);
    }

    @Override
    public void end() throws ReportException {
        this.selectedReportPlugins = selectableReportPlugins;
        run(ReportPlugin::end);
    }

    @Override
    public void beginConcept(Concept concept, Map<Map.Entry<Concept, Boolean>, Result.Status> requiredConceptResults,
        Map<Concept, Result.Status> providingConceptResults) throws ReportException {
        this.selectedReportPlugins = selectReportPlugins(concept);
        run(reportPlugin -> reportPlugin.beginConcept(concept, requiredConceptResults, providingConceptResults));
    }

    @Override
    public void beginConcept(final Concept concept) throws ReportException {
        this.selectedReportPlugins = selectReportPlugins(concept);
        run(reportPlugin -> reportPlugin.beginConcept(concept));
    }

    @Override
    public void endConcept() throws ReportException {
        run(ReportPlugin::endConcept);
    }

    @Override
    public void beginGroup(final Group group) throws ReportException {
        this.selectedReportPlugins = Collections.emptyMap();
        run(reportPlugin -> reportPlugin.beginGroup(group));
    }

    @Override
    public void endGroup() throws ReportException {
        run(ReportPlugin::endGroup);
    }

    @Override
    public void beginConstraint(Constraint constraint, Map<Map.Entry<Concept, Boolean>, Result.Status> requiredConceptResults) throws ReportException {
        this.selectedReportPlugins = selectReportPlugins(constraint);
        run(reportPlugin -> reportPlugin.beginConstraint(constraint, requiredConceptResults));
    }

    @Override
    public void beginConstraint(final Constraint constraint) throws ReportException {
        this.selectedReportPlugins = selectReportPlugins(constraint);
        run(reportPlugin -> reportPlugin.beginConstraint(constraint));
    }

    @Override
    public void endConstraint() throws ReportException {
        run(ReportPlugin::endConstraint);
    }

    @Override
    public void setResult(final Result<? extends ExecutableRule> result) throws ReportException {
        run(reportPlugin -> reportPlugin.setResult(result));
    }

    /**
     * Execute the {@link ReportOperation} on the selected and default
     * {@link ReportPlugin}s.
     *
     * @param operation
     *     The {@link ReportOperation}.
     * @throws ReportException
     *     If a problem is reported.
     */
    private void run(ReportOperation operation) throws ReportException {
        Set<String> executedPlugins = new HashSet<>();
        run(selectedReportPlugins, operation, executedPlugins);
        run(defaultReportPlugins, operation, executedPlugins);
    }

    /**
     * Execute a {@link ReportOperation} on the provided {@link ReportPlugin}s but
     * assure that this happens only once.
     *
     * @param reportPlugins
     *     The {@link ReportPlugin}s.
     * @param operation
     *     The {@link ReportOperation}.
     * @param executedPlugins
     *     The already executed {@link ReportPlugin}s.
     * @throws ReportException
     *     If a problem is reported.
     */
    private void run(Map<String, ReportPlugin> reportPlugins, ReportOperation operation, Set<String> executedPlugins) throws ReportException {
        for (Map.Entry<String, ReportPlugin> entry : reportPlugins.entrySet()) {
            if (executedPlugins.add(entry.getKey())) {
                operation.run(entry.getValue());
            }
        }
    }

    /**
     * Select the report writers for the given rule.
     *
     * @param rule
     *     The rule.
     * @throws ReportException
     *     If no writer exists for a specified id.
     */
    private Map<String, ReportPlugin> selectReportPlugins(ExecutableRule<?> rule) throws ReportException {
        Set<String> selection = rule.getReport()
            .getSelectedTypes();
        Map<String, ReportPlugin> reportPlugins = new HashMap<>();
        if (selection != null) {
            for (String type : selection) {
                ReportPlugin candidate = this.selectableReportPlugins.get(type);
                if (candidate == null) {
                    throw new ReportException(
                        "Unknown report type '" + type + "' selected for '" + rule + "'. Valid report types are " + this.selectedReportPlugins.keySet());
                }
                reportPlugins.put(type, candidate);
            }
        }
        return reportPlugins;
    }
}
