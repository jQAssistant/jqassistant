package com.buschmais.jqassistant.core.plugin.api;

/**
 * Exception thrown by the {@link PluginConfigurationReader}.
 */
public class PluginRepositoryException extends RuntimeException {

    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = -2317389179258285099L;

    /**
     * Constructor.
     *
     * @param message
     *            The message.
     * @param cause
     *            The cause.
     */
    public PluginRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor.
     *
     * @param message
     *            The message.
     */
    public PluginRepositoryException(String message) {
        super(message);
    }
}
