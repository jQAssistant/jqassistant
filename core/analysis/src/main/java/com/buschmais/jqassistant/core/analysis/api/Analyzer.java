package com.buschmais.jqassistant.core.analysis.api;

import java.util.Map;

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
     * @param parameters
     *            The parameters.
     * @throws AnalysisException
     *             If the analysis fails.
     */
    void execute(RuleSet ruleSet, RuleSelection ruleSelection, Map<String, String> parameters) throws AnalysisException;
}
