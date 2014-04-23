package com.buschmais.jqassistant.scm.common;

/**
 * Defines an abstraction for a console.
 */
public interface Console {

    /**
     * Print an info message.
     *
     * @param message
     *            The message.
     */
    void info(String message);

    /**
     * Print a warning message.
     *
     * @param message
     *            The message.
     */
    void warn(String message);

    /**
     * Print an error message.
     *
     * @param message
     *            The message.
     */
    void error(String message);
}
