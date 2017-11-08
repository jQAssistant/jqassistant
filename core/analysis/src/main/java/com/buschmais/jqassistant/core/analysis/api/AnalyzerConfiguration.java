package com.buschmais.jqassistant.core.analysis.api;

import com.buschmais.jqassistant.core.rule.api.executor.RuleSetExecutorConfiguration;

/**
 * Represents the configuration of the analyzer.
 */
public class AnalyzerConfiguration {

    private boolean executeAppliedConcepts = false;

    private RuleSetExecutorConfiguration ruleSetExecutorConfiguration = new RuleSetExecutorConfiguration();

    public boolean isExecuteAppliedConcepts() {
        return executeAppliedConcepts;
    }

    public void setExecuteAppliedConcepts(boolean executeAppliedConcepts) {
        this.executeAppliedConcepts = executeAppliedConcepts;
    }

    public RuleSetExecutorConfiguration getRuleSetExecutorConfiguration() {
        return ruleSetExecutorConfiguration;
    }

    public void setRuleSetExecutorConfiguration(RuleSetExecutorConfiguration ruleSetExecutorConfiguration) {
        this.ruleSetExecutorConfiguration = ruleSetExecutorConfiguration;
    }
}
