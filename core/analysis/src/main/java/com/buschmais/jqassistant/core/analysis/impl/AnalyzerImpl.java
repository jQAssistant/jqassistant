package com.buschmais.jqassistant.core.analysis.impl;

import com.buschmais.jqassistant.core.analysis.api.*;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.store.api.Store;
import org.slf4j.Logger;

/**
 * Implementation of the {@link Analyzer}.
 */
public class AnalyzerImpl implements Analyzer {

    private Store store;

    private AnalysisListener reportWriter;

    private Logger logger;

    /**
     * Constructor.
     * 
     * @param store
     *            The Store to use.
     */
    public AnalyzerImpl(Store store, AnalysisListener reportWriter, Logger log) {
        this.store = store;
        this.reportWriter = reportWriter;
        this.logger = log;
    }

    @Override
    public void execute(RuleSet ruleSet, RuleSelection ruleSelection) throws AnalysisException {
        try {
            reportWriter.begin();
            try {
                AnalyzerVisitor visitor = new AnalyzerVisitor(ruleSet, store, reportWriter, logger);
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
