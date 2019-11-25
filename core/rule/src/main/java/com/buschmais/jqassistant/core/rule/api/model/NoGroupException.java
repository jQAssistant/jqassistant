package com.buschmais.jqassistant.core.rule.api.model;

public class NoGroupException extends NoRuleException {
    public NoGroupException(String groupId) {
        super(groupId);
    }
}
