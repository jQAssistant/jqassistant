package com.buschmais.jqassistant.plugin.rdbms.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("ForeignKey")
public interface ForeignKeyDescriptor extends RdbmsDescriptor, NamedDescriptor {

    @Relation("HAS_FOREIGN_KEY_REFERENCE")
    List<ForeignKeyReferenceDescriptor> getForeignKeyReferences();

    String getDeferrability();

    void setDeferrability(String deferrability);

    String getDeleteRule();

    void setDeleteRule(String deleteRule);

    String getUpdateRule();

    void setUpdateRule(String updateRule);
}
