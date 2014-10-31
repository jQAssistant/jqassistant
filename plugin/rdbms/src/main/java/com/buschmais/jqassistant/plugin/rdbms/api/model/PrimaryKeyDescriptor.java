package com.buschmais.jqassistant.plugin.rdbms.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("PrimaryKey")
public interface PrimaryKeyDescriptor extends IndexDescriptor {

    List<PrimaryKeyOnColumnDescriptor> getPrimaryKeyOnColumns();
}
