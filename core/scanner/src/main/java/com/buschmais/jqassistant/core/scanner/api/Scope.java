package com.buschmais.jqassistant.core.scanner.api;

/**
 * Interface defining a scope, e.g. a Java classpath.
 */
public interface Scope {

    /**
     * Return the prefix of the scope, e.g. "java".
     * 
     * @return The prefix.
     */
    String getPrefix();

    /**
     * Return the name of the scope.
     * 
     * @return The name.
     */
    String getName();

    /**
     * This method is called by the scanner if a new scope is entered and allows the scope
     * to configure the required environment to plugins by pushing values to the
     * scanner context.
     *
     * @param context the {@link ScannerContext} to be configured,
     *                must not be {@code null}
     */
    void onEnter(ScannerContext context);

    /**
     * This method is called by the scanner if it leaves to current scope to
     * remove the provided environment created by {@link #onEnter(ScannerContext)}.
     *
     * @param context the {@link ScannerContext} to be de-configured,
     *                must not be {@code null}
     */
    void onLeave(ScannerContext context);

}
