package com.buschmais.jqassistant.plugin.java.api.model.generics;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaByteCodeDescriptor;
import com.buschmais.xo.api.annotation.Abstract;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.api.annotation.ResultOf.Parameter;
import com.buschmais.xo.neo4j.api.annotation.Cypher;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

@Abstract
@Label("GenericDeclaration")
public interface GenericDeclarationDescriptor extends JavaByteCodeDescriptor, Descriptor {

    @Outgoing
    @Relation
    List<DeclaresTypeParameterDescriptor> getDeclaresTypeParameters();

    @ResultOf
    @Cypher("MATCH (declaration:GenericDeclaration) WHERE id(declaration)=$this " + //
            "MERGE (declaration)-[declaresTypeParameter:DECLARES_TYPE_PARAMETER{index:$index}]->(typeParameter:Java:ByteCode:GenericType:TypeVariable) " + //
            "RETURN declaresTypeParameter")
    DeclaresTypeParameterDescriptor resolveTypeParameter(@Parameter("index") int index);
}
