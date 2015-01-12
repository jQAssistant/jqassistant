package com.buschmais.jqassistant.plugin.javaee6.api.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("DisplayName")
public interface DisplayNameDescriptor extends Descriptor, LangDescriptor, StringValueDescriptor {

}
