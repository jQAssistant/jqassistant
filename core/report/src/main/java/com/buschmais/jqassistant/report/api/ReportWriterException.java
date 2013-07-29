package com.buschmais.jqassistant.report.api;

/**
 * The exception will be thrown by report writer operations to indicate problems.
 */
public class ReportWriterException extends Exception {

    public ReportWriterException(String message) {
        super(message);
    }

    public ReportWriterException(String message, Throwable cause) {
        super(message, cause);
    }
}
