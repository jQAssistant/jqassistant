package com.buschmais.jqassistant.core.report.api;

/**
 * The exception will be thrown by report writer operations to indicate problems.
 */
public class ReportWriterException extends Exception {

	private static final long serialVersionUID = 1L;

	public ReportWriterException(String message) {
        super(message);
    }

    public ReportWriterException(String message, Throwable cause) {
        super(message, cause);
    }
}
