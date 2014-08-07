package com.buschmais.jqassistant.core.report.api;

import com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException;

public class ReportException extends AnalysisListenerException {

    public ReportException(String message) {
        super(message);
    }

    public ReportException(String message, Throwable cause) {
        super(message, cause);
    }
}
