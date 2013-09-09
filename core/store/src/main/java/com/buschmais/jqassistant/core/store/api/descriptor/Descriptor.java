package com.buschmais.jqassistant.core.store.api.descriptor;

/**
 * Base interface for all types descriptors (e.g. classes, fields, methods, etc.)
 */
public interface Descriptor {

    /**
     * Return the id.
     *
     * @return The id.
     */
    Long getId();

    /**
     * Set the id.
     *
     * @param id The id.
     */
    void setId(Long id);

    /**
     * Return the full qualified name.
     *
     * @return The full qualified name.
     */
    String getFullQualifiedName();


    /**
     * Return the full qualified name.
     *
     * @param fullQualifiedName The full qualified name.
     */
    void setFullQualifiedName(String fullQualifiedName);
}
