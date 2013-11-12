package com.buschmais.jqassistant.core.store.api.descriptor;

import com.buschmais.cdo.neo4j.api.annotation.Indexed;
import com.buschmais.cdo.neo4j.api.annotation.Property;

/**
 * Base interface for all indexed descriptors having a property full qualified
 * name
 */
public interface FullQualifiedNameDescriptor extends Descriptor {

    /**
     * Return the full qualified name.
     *
     * @return The full qualified name.
     */
    @Property("FQN")
    @Indexed
    public String getFullQualifiedName();

    /**
     * Set the full qualified name.
     *
     * @param fullQualifiedName The full qualified name.
     */
    public void setFullQualifiedName(String fullQualifiedName);

}
