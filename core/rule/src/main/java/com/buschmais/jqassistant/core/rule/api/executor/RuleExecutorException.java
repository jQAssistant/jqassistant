package com.buschmais.jqassistant.core.rule.api.executor;

import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;

/** Defines the exception thrown by the rule executor. */
public class RuleExecutorException extends RuleException {

    public RuleExecutorException(String message) {
        super(message);
    }

    public RuleExecutorException(String message, Throwable cause) {
        super(message, cause);
    }
}
