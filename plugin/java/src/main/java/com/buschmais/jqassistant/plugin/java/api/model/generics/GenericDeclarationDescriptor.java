package com.buschmais.jqassistant.plugin.java.api.model.generics;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaByteCodeDescriptor;
import com.buschmais.xo.api.annotation.Abstract;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

@Abstract
@Label("GenericDeclaration")
public interface GenericDeclarationDescriptor extends JavaByteCodeDescriptor, Descriptor {

    @Outgoing
    List<GenericDeclarationDeclaresTypeParameter> getDeclaredTypeParameters();

}
