package com.buschmais.jqassistant.plugin.rdbms.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Routine")
public interface RoutineDescriptor extends RdbmsDescriptor, NamedDescriptor {

    String getReturnType();

    void setReturnType(String returnType);

    String getBodyType();

    void setBodyType(String name);

    String getDefinition();

    void setDefinition(String definition);

    @Relation("HAS_COLUMN")
    List<RoutineColumnDescriptor> getColumns();

}
