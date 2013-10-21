package com.buschmais.jqassistant.core.analysis.api;

/**
 * Exception thrown by the
 * {@link com.buschmais.jqassistant.core.pluginmanager.api.PluginReader}.
 */
public class PluginReaderException extends Exception {

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
