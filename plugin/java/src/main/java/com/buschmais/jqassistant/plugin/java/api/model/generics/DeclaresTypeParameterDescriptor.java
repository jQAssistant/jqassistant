package com.buschmais.jqassistant.plugin.java.api.model.generics;

import com.buschmais.jqassistant.plugin.java.api.model.IndexTemplate;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

@Relation("DECLARES_TYPE_PARAMETER")
public interface DeclaresTypeParameterDescriptor extends IndexTemplate {

    @Outgoing
    GenericDeclarationDescriptor getGenericDeclaration();

    @Incoming
    TypeVariableDescriptor getTypeVariable();

}
