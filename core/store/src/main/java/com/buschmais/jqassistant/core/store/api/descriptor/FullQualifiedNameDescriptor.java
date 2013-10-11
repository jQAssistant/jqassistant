package com.buschmais.jqassistant.core.store.api.descriptor;

/**
 * Base interface for all indexed descriptors having a property full qualified name
 */
public interface FullQualifiedNameDescriptor extends Descriptor {

    /**
     * Return the full qualified name.
     *
     * @return The full qualified name.
     */
    public String getFullQualifiedName();

    /**
     * Set the full qualified name.
     *
     * @param fullQualifiedName The full qualified name.
     */
    public void setFullQualifiedName(String fullQualifiedName);

}
