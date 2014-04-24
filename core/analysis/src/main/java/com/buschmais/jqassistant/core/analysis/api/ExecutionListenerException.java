package com.buschmais.jqassistant.core.analysis.api;

/**
 * The exception will be thrown by report writer operations to indicate
 * problems.
 */
public class ExecutionListenerException extends Exception {

    private static final long serialVersionUID = 1L;

    public ExecutionListenerException(String message) {
        super(message);
    }

    public ExecutionListenerException(String message, Throwable cause) {
        super(message, cause);
    }
}
