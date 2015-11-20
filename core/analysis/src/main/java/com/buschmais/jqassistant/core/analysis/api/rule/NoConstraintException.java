package com.buschmais.jqassistant.core.analysis.api.rule;

public class NoConstraintException extends NoRuleException {
    public NoConstraintException(String conceptId) {
        super(conceptId);
    }
}
