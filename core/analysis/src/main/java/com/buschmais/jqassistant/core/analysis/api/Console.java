package com.buschmais.jqassistant.core.analysis.api;

/**
 * Defines an abstraction for a console.
 * @deprecated Must be replaced with Slf4j
 */
@Deprecated
public interface Console {

    /**
     * Print a debug message.
     *
     * @param message
     *            The message.
     */
    void debug(String message);

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
