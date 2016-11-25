package com.buschmais.jqassistant.core.analysis.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the configuration of the analyzer.
 */
public class AnalyzerConfiguration {

    private Map<String, String> ruleParameters = new HashMap<>();

    private boolean executeAppliedConcepts = false;

    public Map<String, String> getRuleParameters() {
        return ruleParameters;
    }

    public boolean isExecuteAppliedConcepts() {
        return executeAppliedConcepts;
    }

    public void setExecuteAppliedConcepts(boolean executeAppliedConcepts) {
        this.executeAppliedConcepts = executeAppliedConcepts;
    }
}
