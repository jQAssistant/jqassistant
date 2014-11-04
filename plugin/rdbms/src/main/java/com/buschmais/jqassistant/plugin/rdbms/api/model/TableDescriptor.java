package com.buschmais.jqassistant.plugin.rdbms.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Table")
public interface TableDescriptor extends RdbmsDescriptor, NamedDescriptor {

    @Relation("HAS_PRIMARY_KEY")
    PrimaryKeyDescriptor getPrimaryKey();

    void setPrimaryKey(PrimaryKeyDescriptor primaryKeyDescriptor);

    @Relation("HAS_COLUMN")
    List<ColumnDescriptor> getColumns();

    @Relation("HAS_INDEX")
    List<IndexDescriptor> getIndices();

    @Relation("HAS_TRIGGER")
    List<TriggerDescriptor> getTriggers();
}
