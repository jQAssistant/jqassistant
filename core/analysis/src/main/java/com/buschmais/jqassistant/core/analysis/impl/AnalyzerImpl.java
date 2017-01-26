package com.buschmais.jqassistant.core.analysis.impl;

import com.buschmais.jqassistant.core.analysis.api.*;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSelection;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.rule.api.executor.RuleExecutor;
import com.buschmais.jqassistant.core.rule.api.executor.RuleExecutorException;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.store.api.Store;

import org.slf4j.Logger;

import java.util.Map;

/**
 * Implementation of the {@link Analyzer}.
 */
public class AnalyzerImpl implements Analyzer {

    private AnalyzerConfiguration configuration;

    private Store store;

    private ReportPlugin reportPlugin;

    private Logger logger;

    /**
     * Constructor.
     *
     * @param configuration
     *            The configuration.
     * @param store
     *            The store
     * @param reportPlugin
     *            The report wrtier.
     * @param log
     *            The logger.
     */
    public AnalyzerImpl(AnalyzerConfiguration configuration, Store store, ReportPlugin reportPlugin, Logger log) {
        this.configuration = configuration;
        this.store = store;
        this.reportPlugin = reportPlugin;
        this.logger = log;
    }

    @Override
    public void execute(RuleSet ruleSet, RuleSelection ruleSelection, Map<String, String> ruleParameters) throws RuleExecutorException {
        reportPlugin.begin();
        try {
            AnalyzerVisitor visitor = new AnalyzerVisitor(configuration, ruleParameters, store, reportPlugin, logger);
            RuleExecutor executor = new RuleExecutor(visitor, configuration.getRuleExecutorConfiguration());
            executor.execute(ruleSet, ruleSelection);
        } finally {
            reportPlugin.end();
        }
    }

}
