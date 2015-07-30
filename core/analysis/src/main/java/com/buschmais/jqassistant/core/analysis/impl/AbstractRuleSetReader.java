package com.buschmais.jqassistant.core.analysis.impl;

import com.buschmais.jqassistant.core.analysis.api.RuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSetBuilder;

/**
 * Abstract base class for rule set readers.
 */
public abstract class AbstractRuleSetReader implements RuleSetReader {

    private RuleSetBuilder ruleSetBuilder;

    /**
     * Constructor.
     * 
     * @param ruleSetBuilder
     *            The rule set builder instance.
     */
    protected AbstractRuleSetReader(RuleSetBuilder ruleSetBuilder) {
        this.ruleSetBuilder = ruleSetBuilder;
    }

    protected RuleSetBuilder getRuleSetBuilder() {
        return ruleSetBuilder;
    }
}
