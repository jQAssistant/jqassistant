package com.buschmais.jqassistant.core.analysis.api;

/**
 * Exception thrown by the {@link Analyzer}.
 */
public class AnalyzerException extends Exception {

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            The message.
	 * @param cause
	 *            The cause.
	 */
	public AnalyzerException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            The message.
	 */
	public AnalyzerException(String message) {
		super(message);
	}
}
