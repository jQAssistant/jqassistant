package com.buschmais.jqassistant.core.rule.api.model;

public class NoRuleException extends RuleHandlingException {
    public NoRuleException(String id) {
        super("Rule with id '" + id + "' not found.");
    }
}
