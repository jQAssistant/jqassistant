package com.buschmais.jqassistant.core.rule.api.model;

public class RuleException extends Exception {

    public RuleException(String message) {
        super(message);
    }

    public RuleException(String message, Throwable cause) {
        super(message, cause);
    }

}
