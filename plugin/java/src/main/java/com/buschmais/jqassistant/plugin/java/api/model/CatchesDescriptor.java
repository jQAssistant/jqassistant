package com.buschmais.jqassistant.plugin.java.api.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

@Relation("CATCHES")
public interface CatchesDescriptor extends Descriptor {

    @Outgoing
    MethodDescriptor getMethod();

    @Incoming
    TypeDescriptor getExceptionType();

    Integer getFirstLineNumber();

    void setFirstLineNumber(Integer firstLineNumber);

    Integer getLastLineNumber();

    void setLastLineNumber(Integer lastLineNumber);
}
