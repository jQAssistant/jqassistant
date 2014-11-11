package com.buschmais.jqassistant.plugin.rdbms.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Schema")
public interface SchemaDescriptor extends RdbmsDescriptor, NamedDescriptor {

    @Relation("HAS_TABLE")
    List<TableDescriptor> getTables();

    @Relation("HAS_VIEW")
    List<ViewDescriptor> getViews();

    @Relation("HAS_SEQUENCE")
    List<SequenceDesriptor> getSequences();

    @Relation("HAS_FUNCTION")
    List<FunctionDescriptor> getFunctions();

    @Relation("HAS_PROCEDURE")
    List<ProcedureDescriptor> getProcedures();

    @Relation("HAS_UNKNOWN_ROUTINE")
    List<RoutineDescriptor> getUnknownRoutines();
}
