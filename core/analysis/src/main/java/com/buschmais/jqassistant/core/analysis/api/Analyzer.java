package com.buschmais.jqassistant.core.analysis.api;

import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.rule.RuleSelection;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.rule.api.executor.RuleExecutorException;

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
     */
    void execute(RuleSet ruleSet, RuleSelection ruleSelection, Map<String, String> parameters) throws RuleExecutorException;
}
