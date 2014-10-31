package com.buschmais.jqassistant.plugin.rdbms.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("Index")
public interface IndexDescriptor extends RdbmsDescriptor, NamedDescriptor {

    List<IndexOnColumnDescriptor> getIndexOnColumns();

    boolean isUnique();

    void setUnique(boolean unique);

    int getCardinality();

    void setCardinality(int cardinality);

    String getIndexType();

    void setIndexType(String name);

    int getPages();

    void setPages(int pages);
}
