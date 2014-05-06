package com.buschmais.jqassistant.core.analysis.api;

/**
 * Exception thrown by the
 * {@link com.buschmais.jqassistant.core.pluginrepository.api.PluginReader}.
 */
public class PluginReaderException extends Exception {

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
    public PluginReaderException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor.
     * 
     * @param message
     *            The message.
     */
    public PluginReaderException(String message) {
        super(message);
    }
}
