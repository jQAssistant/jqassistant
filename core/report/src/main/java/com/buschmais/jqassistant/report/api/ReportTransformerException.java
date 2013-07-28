package com.buschmais.jqassistant.report.api;

/**
 * The exception will be thrown by report transformer operations to indicate problems.
 */
public class ReportTransformerException extends Exception {

    public ReportTransformerException(String message) {
        super(message);
    }

    public ReportTransformerException(String message, Throwable cause) {
        super(message, cause);
    }
}
