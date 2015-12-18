package com.buschmais.jqassistant.plugin.javaee6.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("HttpMethodOmission")
public interface HttpMethodOmissionDescriptor extends WebDescriptor, NamedDescriptor {
}
