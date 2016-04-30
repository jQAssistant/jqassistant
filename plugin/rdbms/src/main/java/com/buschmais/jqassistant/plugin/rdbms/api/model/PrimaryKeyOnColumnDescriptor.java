package com.buschmais.jqassistant.plugin.rdbms.api.model;

import com.buschmais.xo.neo4j.api.annotation.Relation;

import static com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import static com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

@Relation("ON_PRIMARY_KEY_COLUMN")
public interface PrimaryKeyOnColumnDescriptor extends OnColumnDescriptor {

    @Incoming
    ColumnDescriptor getColumn();

    @Outgoing
    PrimaryKeyDescriptor getPrimaryKey();

}
