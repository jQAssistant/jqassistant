package com.buschmais.jqassistant.core.plugin.api;

import com.buschmais.jqassistant.core.scanner.api.Scope;

/**
 * Defines the interface for the scope plugin repository.
 * <p>
 * A scope can be retrieved by it's fully qualified name, e.g. "java:classpath".
 * </p>
 */
public interface ScopePluginRepository {

    /**
     * Return the scope for the given name.
     * 
     * @param name
     *            The name.
     * @return The scope.
     */
    Scope getScope(String name);

}
