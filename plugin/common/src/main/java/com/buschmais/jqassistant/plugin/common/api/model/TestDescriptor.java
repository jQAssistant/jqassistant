package com.buschmais.jqassistant.plugin.common.api.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Label denoting test related nodes
 */
@Label("Test")
public interface TestDescriptor extends Descriptor {
}
