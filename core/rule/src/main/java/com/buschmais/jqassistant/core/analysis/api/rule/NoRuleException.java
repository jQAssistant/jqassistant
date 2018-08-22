package com.buschmais.jqassistant.core.analysis.api.rule;

public class NoRuleException extends RuleHandlingException {
    public NoRuleException(String id) {
        super("Rule with id '" + id + "' not found.");
    }
}
