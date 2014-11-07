package com.buschmais.jqassistant.core.analysis.api;

import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;

/**
 * Defines the interface for the analyzer.
 */
public interface Analyzer {

    /**
     * Executes the given rule set.
     * 
     * @param ruleSet
     *            The rules set.
     * @param ruleSelection
     *            The rule selection.
     * @throws AnalysisException
     *             If the analysis fails.
     */
    void execute(RuleSet ruleSet, RuleSelection ruleSelection) throws AnalysisException;
}
