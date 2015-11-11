package com.buschmais.jqassistant.core.analysis.api.rule;

public class NoRuleException extends RuleHandlingException {
    public NoRuleException(String conceptId) {
        super("Concept " + conceptId + " not found.");
    }
}
