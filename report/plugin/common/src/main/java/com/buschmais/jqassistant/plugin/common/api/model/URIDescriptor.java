package com.buschmais.jqassistant.plugin.common.api.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.api.annotation.Abstract;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Represents a URI.
 */
@Label("URI")
@Abstract
public interface URIDescriptor extends Descriptor {

    String getUri();
    void setUri(String uri);

}
