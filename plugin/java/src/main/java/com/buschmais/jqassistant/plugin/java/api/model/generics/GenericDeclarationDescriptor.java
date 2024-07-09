package com.buschmais.jqassistant.plugin.java.api.model.generics;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaByteCodeDescriptor;
import com.buschmais.xo.api.annotation.Abstract;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.api.annotation.ResultOf.Parameter;
import com.buschmais.xo.neo4j.api.annotation.Cypher;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Abstract
@Label("GenericDeclaration")
public interface GenericDeclarationDescriptor extends JavaByteCodeDescriptor, Descriptor {

    @ResultOf
    @Cypher("MATCH (declaration:GenericDeclaration) WHERE id(declaration)=$this " + //
            "MERGE (declaration)-[:DECLARES_TYPE_PARAMETER{index:$index}]->(typeParameter:Java:ByteCode:Bound:TypeVariable) " + //
            "RETURN typeParameter")
    TypeVariableDescriptor resolveTypeParameter(@Parameter("index") int index);
}
