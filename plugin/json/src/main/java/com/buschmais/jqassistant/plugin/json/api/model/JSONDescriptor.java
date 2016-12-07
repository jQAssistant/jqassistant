package com.buschmais.jqassistant.plugin.json.api.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.api.annotation.Abstract;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Represents a part of a JSON document.
 */
@Abstract
@Label("JSON")
public interface JSONDescriptor extends Descriptor {

}
