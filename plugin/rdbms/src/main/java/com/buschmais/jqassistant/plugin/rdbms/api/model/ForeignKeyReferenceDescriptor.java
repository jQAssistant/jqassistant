package com.buschmais.jqassistant.plugin.rdbms.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("ForeignKeyReference")
public interface ForeignKeyReferenceDescriptor extends RdbmsDescriptor {

    @Relation("FROM_FOREIGN_KEY_COLUMN")
    ColumnDescriptor getForeignKeyColumn();

    void setForeignKeyColumn(ColumnDescriptor foreignKeyColumn);

    @Relation("TO_PRIMARY_KEY_COLUMN")
    ColumnDescriptor getPrimaryKeyColumn();

    void setPrimaryKeyColumn(ColumnDescriptor primaryKeyColumn);

}
