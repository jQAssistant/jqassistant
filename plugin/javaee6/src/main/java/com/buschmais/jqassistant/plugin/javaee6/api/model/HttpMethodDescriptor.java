package com.buschmais.jqassistant.plugin.javaee6.api.model;

import com.buschmais.jqassistant.core.store.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("HttpMethod")
public interface HttpMethodDescriptor extends WebDescriptor, NamedDescriptor {
}
