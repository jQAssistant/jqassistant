package com.buschmais.jqassistant.core.analysis.api.rule;

public class NoConceptException extends NoRuleException {
    public NoConceptException(String conceptId) {
        super(conceptId);
    }
}
