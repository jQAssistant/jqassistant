package com.buschmais.jqassistant.core.plugin.api;

import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.Scope;

/**
 * Defines the interface for the scope plugin repository.
 *
 * A scope can be retrieved by it's fully qualified name, e.g. `java:classpath`.
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

    /**
     * Return a map of all scopes identified by their fully qualified name.
     * 
     * @return The map of all scopes.
     */
    Map<String, Scope> getScopes();
}
