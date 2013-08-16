package com.buschmais.jqassistant.core.analysis.api;

/**
 * Exception thrown by the {@link RuleSetResolver}.
 */
public class RuleSetResolverException extends Exception {

    /**
     * Constructor.
     *
     * @param message The message.
     * @param cause   The cause.
     */
    public RuleSetResolverException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor.
     *
     * @param message The message.
     */
    public RuleSetResolverException(String message) {
        super(message);
    }
}
