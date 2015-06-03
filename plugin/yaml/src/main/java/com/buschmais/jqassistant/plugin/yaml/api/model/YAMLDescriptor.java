package com.buschmais.jqassistant.plugin.yaml.api.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.api.annotation.Abstract;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Abstract
@Label("YAML")
public interface YAMLDescriptor extends Descriptor {

}
