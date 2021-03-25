package com.buschmais.jqassistant.core.analysis.api;

import com.buschmais.jqassistant.core.rule.api.executor.RuleSetExecutorConfiguration;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents the configuration of the analyzer.
 */
@Getter
@Setter
public class AnalyzerConfiguration {

    private boolean executeAppliedConcepts = false;

    private RuleSetExecutorConfiguration ruleSetExecutorConfiguration = new RuleSetExecutorConfiguration();

    private int warnOnRuleExecutionTimeSeconds = 5;

}
