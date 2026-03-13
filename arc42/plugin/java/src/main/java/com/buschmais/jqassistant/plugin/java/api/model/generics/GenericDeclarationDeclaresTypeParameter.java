package com.buschmais.jqassistant.plugin.java.api.model.generics;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

@Relation("DECLARES_TYPE_PARAMETER")
public interface GenericDeclarationDeclaresTypeParameter extends Descriptor {

    @Incoming
    TypeVariableDescriptor getTypeParameter();

    @Outgoing
    GenericDeclarationDescriptor getDeclaration();

    int getIndex();

    void setIndex(int index);

}
