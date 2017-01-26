package com.buschmais.jqassistant.core.analysis.api;

import com.buschmais.jqassistant.core.rule.api.executor.RuleExecutorConfiguration;

/**
 * Represents the configuration of the analyzer.
 */
public class AnalyzerConfiguration {

    private boolean executeAppliedConcepts = false;

    private RuleExecutorConfiguration ruleExecutorConfiguration = new RuleExecutorConfiguration();

    public boolean isExecuteAppliedConcepts() {
        return executeAppliedConcepts;
    }

    public void setExecuteAppliedConcepts(boolean executeAppliedConcepts) {
        this.executeAppliedConcepts = executeAppliedConcepts;
    }

    public RuleExecutorConfiguration getRuleExecutorConfiguration() {
        return ruleExecutorConfiguration;
    }

    public void setRuleExecutorConfiguration(RuleExecutorConfiguration ruleExecutorConfiguration) {
        this.ruleExecutorConfiguration = ruleExecutorConfiguration;
    }
}
