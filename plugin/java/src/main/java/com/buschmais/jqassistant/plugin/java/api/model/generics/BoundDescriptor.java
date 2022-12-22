package com.buschmais.jqassistant.plugin.java.api.model.generics;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaByteCodeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;

@Label("Bound")
public interface BoundDescriptor extends JavaByteCodeDescriptor, Descriptor {

    @Relation("OF_RAW_TYPE")
    TypeDescriptor getRawType();

    void setRawType(TypeDescriptor rawType);

    @Incoming
    List<HasActualTypeArgumentDescriptor> getParameterizedType();

}
