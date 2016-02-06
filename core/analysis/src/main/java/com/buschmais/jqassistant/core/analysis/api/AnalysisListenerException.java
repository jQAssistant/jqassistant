package com.buschmais.jqassistant.core.analysis.api;

/**
 * The exception thrown by an instance of a
 * {@link com.buschmais.jqassistant.core.analysis.api.AnalysisListener}.
 *
 * @see AnalysisListener
 */
public class AnalysisListenerException extends AnalysisException {

    private static final long serialVersionUID = 1L;

    public AnalysisListenerException(String message) {
        super(message);
    }

    public AnalysisListenerException(String message, Throwable cause) {
        super(message, cause);
    }
}
