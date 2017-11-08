package com.buschmais.jqassistant.core.report.api;

import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;

public class ReportException extends RuleException {

    public ReportException(String message) {
        super(message);
    }

    public ReportException(String message, Throwable cause) {
        super(message, cause);
    }
}
