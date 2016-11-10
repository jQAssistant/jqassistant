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

}
