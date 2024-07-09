package com.buschmais.jqassistant.plugin.common.test.mapper;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("Model")
public interface ModelDescriptor extends Descriptor {

    String getName();
    void setName(String name);

}
