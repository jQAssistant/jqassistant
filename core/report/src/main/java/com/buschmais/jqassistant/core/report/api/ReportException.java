package com.buschmais.jqassistant.core.report.api;

import com.buschmais.jqassistant.core.rule.api.executor.RuleExecutorException;

public class ReportException extends RuleExecutorException {

    public ReportException(String message) {
        super(message);
    }

    public ReportException(String message, Throwable cause) {
        super(message, cause);
    }
}
