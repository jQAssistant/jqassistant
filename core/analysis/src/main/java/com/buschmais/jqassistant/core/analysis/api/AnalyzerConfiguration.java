package com.buschmais.jqassistant.core.analysis.api;

/**
 * Represents the configuration of the analyzer.
 */
public class AnalyzerConfiguration {

    private boolean executeAppliedConcepts = false;

    public boolean isExecuteAppliedConcepts() {
        return executeAppliedConcepts;
    }

    public void setExecuteAppliedConcepts(boolean executeAppliedConcepts) {
        this.executeAppliedConcepts = executeAppliedConcepts;
    }
}
