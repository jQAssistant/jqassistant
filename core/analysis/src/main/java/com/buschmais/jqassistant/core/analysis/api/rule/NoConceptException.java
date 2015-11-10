package com.buschmais.jqassistant.core.analysis.api.rule;

public class NoConceptException extends RuleHandlingException {
    public NoConceptException(String conceptId) {
        super("Concept " + conceptId + " not found.");
    }
}
