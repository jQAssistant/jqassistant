package com.buschmais.jqassistant.core.analysis.api;

import java.util.Map;

import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSelection;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;

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
    void execute(RuleSet ruleSet, RuleSelection ruleSelection, Map<String, String> parameters) throws RuleException;
}
