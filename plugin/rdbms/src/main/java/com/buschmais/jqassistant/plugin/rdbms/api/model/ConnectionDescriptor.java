package com.buschmais.jqassistant.plugin.rdbms.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Connection")
public interface ConnectionDescriptor extends RdbmsDescriptor {

    @Relation("DESCRIBES_SCHEMA")
    List<SchemaDescriptor> getSchemas();

}
