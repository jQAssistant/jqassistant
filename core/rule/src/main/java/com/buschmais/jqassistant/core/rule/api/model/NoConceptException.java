package com.buschmais.jqassistant.core.rule.api.model;

public class NoConceptException extends NoRuleException {
    public NoConceptException(String conceptId) {
        super(conceptId);
    }
}
