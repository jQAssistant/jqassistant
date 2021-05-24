package com.buschmais.jqassistant.plugin.java.api.model.generics;

import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;

@Label("TypeVariable")
public interface TypeVariableDescriptor extends GenericTypeDescriptor, NamedDescriptor {

    @Incoming
    @Relation
    DeclaresTypeParameterDescriptor getDeclaredBy();

    @Relation("HAS_BOUND")
    GenericTypeDescriptor getBounds();

}
