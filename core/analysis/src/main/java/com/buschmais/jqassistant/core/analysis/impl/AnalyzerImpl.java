package com.buschmais.jqassistant.core.analysis.impl;

import com.buschmais.jqassistant.core.analysis.api.*;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.store.api.Store;

/**
 * Implementation of the {@link Analyzer}.
 */
public class AnalyzerImpl implements Analyzer {

    private Store store;

    private AnalysisListener reportWriter;

    private Console console;

    /**
     * Constructor.
     * 
     * @param store
     *            The Store to use.
     */
    public AnalyzerImpl(Store store, AnalysisListener reportWriter, Console console) {
        this.store = store;
        this.reportWriter = reportWriter;
        this.console = console;
    }

    @Override
    public void execute(RuleSet ruleSet, RuleSelection ruleSelection) throws AnalysisException {
        try {
            reportWriter.begin();
            try {
                AnalyzerVisitor visitor = new AnalyzerVisitor(ruleSet, store, reportWriter, console);
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
