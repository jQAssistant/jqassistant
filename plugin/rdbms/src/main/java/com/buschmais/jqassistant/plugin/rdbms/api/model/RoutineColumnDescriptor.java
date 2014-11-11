package com.buschmais.jqassistant.plugin.rdbms.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("RoutineColumn")
public interface RoutineColumnDescriptor extends RdbmsDescriptor, BaseColumnDescriptor {

    String getType();

    void setType(String type);

}
