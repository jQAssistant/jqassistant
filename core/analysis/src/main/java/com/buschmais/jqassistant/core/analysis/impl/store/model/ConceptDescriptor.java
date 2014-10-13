package com.buschmais.jqassistant.core.analysis.impl.store.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Indexed;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Describes an executed concept.
 */
@Label("Concept")
public interface ConceptDescriptor extends Descriptor {

    @Indexed
    String getId();

    void setId(String id);
}
