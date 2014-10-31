package com.buschmais.jqassistant.plugin.rdbms.api.model;

import static com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import static com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Relation("ON_INDEX_COLUMN")
public interface IndexOnColumnDescriptor extends Descriptor, OnColumnDescriptor {

    @Incoming
    ColumnDescriptor getColumn();

    @Outgoing
    IndexDescriptor getIndex();

}
