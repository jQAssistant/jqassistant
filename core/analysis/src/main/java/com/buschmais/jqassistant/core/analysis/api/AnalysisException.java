package com.buschmais.jqassistant.core.analysis.api;

/**
 * Exception thrown by the {@link Analyzer}.
 */
public class AnalysisException extends Exception {

    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = 4006069318251056205L;

    /**
     * Constructor.
     * 
     * @param message
     *            The message.
     * @param cause
     *            The cause.
     */
    public AnalysisException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor.
     * 
     * @param message
     *            The message.
     */
    public AnalysisException(String message) {
        super(message);
    }
}
