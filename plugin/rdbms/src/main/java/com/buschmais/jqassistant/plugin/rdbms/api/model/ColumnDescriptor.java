package com.buschmais.jqassistant.plugin.rdbms.api.model;

import com.buschmais.jqassistant.core.store.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("Column")
public interface ColumnDescriptor extends RdbmsDescriptor, NamedDescriptor {
}
