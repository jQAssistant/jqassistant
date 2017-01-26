package com.buschmais.jqassistant.core.analysis.api.rule;

public class RuleException extends Exception {

    public RuleException(String message) {
        super(message);
    }

    public RuleException(String message, Throwable cause) {
        super(message, cause);
    }

}
