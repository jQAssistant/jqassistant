package com.buschmais.jqassistant.core.rule.api.model;

public class NoConstraintException extends NoRuleException {
    public NoConstraintException(String conceptId) {
        super(conceptId);
    }
}
