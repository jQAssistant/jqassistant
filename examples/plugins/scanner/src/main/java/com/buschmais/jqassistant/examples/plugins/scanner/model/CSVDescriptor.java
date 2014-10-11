package com.buschmais.jqassistant.examples.plugins.scanner.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Defines the label which is shared by all nodes representing CSV structures.
 */
@Label("CSV")
public interface CSVDescriptor extends Descriptor {
}
