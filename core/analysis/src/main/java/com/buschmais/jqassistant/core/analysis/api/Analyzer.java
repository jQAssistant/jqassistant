package com.buschmais.jqassistant.core.analysis.api;

import com.buschmais.jqassistant.core.analysis.api.configuration.Analyze;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSelection;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;

/**
 * Defines the interface for the analyzer.
 */
public interface Analyzer {

    /**
     * Return the {@link Analyze} configuration.
     *
     * @return The {@link Analyze} configuration.
     */
    Analyze getConfiguration();

    /**
     * Executes the given rule set.
     *
     * @param ruleSet
     *     The rules set.
     * @param ruleSelection
     *     The rule selection.
     */
    void execute(RuleSet ruleSet, RuleSelection ruleSelection) throws RuleException;
}
