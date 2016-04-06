package com.buschmais.jqassistant.core.analysis.impl;

import org.slf4j.Logger;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.api.AnalysisListener;
import com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException;
import com.buschmais.jqassistant.core.analysis.api.Analyzer;
import com.buschmais.jqassistant.core.analysis.api.AnalyzerConfiguration;
import com.buschmais.jqassistant.core.analysis.api.RuleSelection;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.store.api.Store;

/**
 * Implementation of the {@link Analyzer}.
 */
public class AnalyzerImpl implements Analyzer {

    private AnalyzerConfiguration configuration;

    private Store store;

    private AnalysisListener reportWriter;

    private Logger logger;

    /**
     * Constructor.
     *
     * @param configuration
     *            The configuration.
     * @param store
     *            The store
     * @param reportWriter
     *            The report wrtier.
     * @param log
     *            The logger.
     */
    public AnalyzerImpl(AnalyzerConfiguration configuration, Store store, AnalysisListener reportWriter, Logger log) {
        this.configuration = configuration;
        this.store = store;
        this.reportWriter = reportWriter;
        this.logger = log;
    }

    @Override
    public void execute(RuleSet ruleSet, RuleSelection ruleSelection) throws AnalysisException {
        try {
            reportWriter.begin();
            try {
                AnalyzerVisitor visitor = new AnalyzerVisitor(ruleSet, configuration.isExecuteAppliedConcepts(), store, reportWriter, logger);
                RuleExecutor executor = new RuleExecutor(visitor);
                executor.execute(ruleSet, ruleSelection);
            } finally {
                reportWriter.end();
            }
        } catch (AnalysisListenerException e) {
            throw new AnalysisException("Cannot write report.", e);
        }
    }

}
