package com.buschmais.jqassistant.plugins.json.api.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.api.annotation.Abstract;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Abstract
@Label("JSON")
public interface JSONDescriptor extends Descriptor {

}
