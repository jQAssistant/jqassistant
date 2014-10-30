package com.buschmais.jqassistant.plugin.rdbms.api.model;

import java.util.List;

import com.buschmais.jqassistant.plugin.java.api.model.PropertyFileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Connection")
public interface ConnectionPropertiesDescriptor extends PropertyFileDescriptor, RdbmsDescriptor {

    @Relation("DESCRIBES_SCHEMA")
    List<SchemaDescriptor> getSchemas();

}
