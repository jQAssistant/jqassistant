package com.buschmais.jqassistant.core.analysis.api;

/**
 * Represents the configuration of the rule executor.
 */
public class RuleExecutorConfiguration {

    private boolean requiredConceptsAreOptionalByDefault = true;

    public boolean isRequiredConceptsAreOptionalByDefault() {
        return requiredConceptsAreOptionalByDefault;
    }

    public void setRequiredConceptsAreOptionalByDefault(boolean requiredConceptsAreOptionalByDefault) {
        this.requiredConceptsAreOptionalByDefault = requiredConceptsAreOptionalByDefault;
    }
}
