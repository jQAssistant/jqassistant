package com.buschmais.jqassistant.core.store.api.model;

import com.buschmais.xo.neo4j.api.annotation.Indexed;
import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * Base interface for all indexed descriptors having a property full qualified
 * name.
 */
public interface FullQualifiedNameDescriptor extends Descriptor {

    /**
     * Return the full qualified name.
     *
     * @return The full qualified name.
     */
    @Property("fqn")
    @Indexed(create = true)
    public String getFullQualifiedName();

    /**
     * Set the full qualified name.
     *
     * @param fullQualifiedName
     *            The full qualified name.
     */
    public void setFullQualifiedName(String fullQualifiedName);

}
