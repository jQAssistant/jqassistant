package com.buschmais.jqassistant.core.model.api.descriptor;

/**
 * Base interface for all type descriptors (e.g. classes, fields, methods, etc.)
 */
public interface Descriptor {

    /**
     * Return the id.
     *
     * @return The id.
     */
    Long getId();

    /**
     * Return the full qualified name.
     *
     * @return The full qualified name.
     */
    String getFullQualifiedName();
}
