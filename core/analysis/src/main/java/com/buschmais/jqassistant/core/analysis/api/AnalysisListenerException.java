package com.buschmais.jqassistant.core.analysis.api;

/**
 * The exception will be thrown
 * {@link com.buschmais.jqassistant.core.analysis.api.AnalysisListener}s.
 */
public class AnalysisListenerException extends Exception {

    private static final long serialVersionUID = 1L;

    public AnalysisListenerException(String message) {
        super(message);
    }

    public AnalysisListenerException(String message, Throwable cause) {
        super(message, cause);
    }
}
