package com.buschmais.jqassistant.core.rule.api.executor;

/**
 * Represents the configuration of the rule executor.
 */
public class RuleSetExecutorConfiguration {

    private boolean requiredConceptsAreOptionalByDefault = true;

    public boolean isRequiredConceptsAreOptionalByDefault() {
        return requiredConceptsAreOptionalByDefault;
    }

    public void setRequiredConceptsAreOptionalByDefault(boolean requiredConceptsAreOptionalByDefault) {
        this.requiredConceptsAreOptionalByDefault = requiredConceptsAreOptionalByDefault;
    }
}
