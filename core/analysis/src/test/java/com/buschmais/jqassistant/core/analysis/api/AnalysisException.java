package com.buschmais.jqassistant.core.analysis.api;

import com.buschmais.jqassistant.core.report.api.ReportException;

public class AnalysisException extends ReportException {

    public AnalysisException(String message) {
        super(message);
    }

    public AnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}
