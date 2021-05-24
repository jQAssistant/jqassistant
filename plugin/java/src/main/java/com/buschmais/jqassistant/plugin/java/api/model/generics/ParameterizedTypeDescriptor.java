package com.buschmais.jqassistant.plugin.java.api.model.generics;

import java.util.List;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("ParameterizedType")
public interface ParameterizedTypeDescriptor extends GenericTypeDescriptor {

    @Relation("HAS_ACTUAL_TYPE_ARGUMENT")
    List<GenericTypeDescriptor> getActualTypeArguments();

    @Relation("HAS_RAW_TYPE")
    TypeDescriptor getRawType();

    void setRawType(TypeDescriptor rawType);

}
