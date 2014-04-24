package com.buschmais.jqassistant.core.analysis.api;

/**
 * Exception thrown by the {@link RuleSelector}.
 */
public class RuleSetResolverException extends Exception {

    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = -3544634548324858170L;

    /**
     * Constructor.
     * 
     * @param message
     *            The message.
     * @param cause
     *            The cause.
     */
    public RuleSetResolverException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor.
     * 
     * @param message
     *            The message.
     */
    public RuleSetResolverException(String message) {
        super(message);
    }
}
