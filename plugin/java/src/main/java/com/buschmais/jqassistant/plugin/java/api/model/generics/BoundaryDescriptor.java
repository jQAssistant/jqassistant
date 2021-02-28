package com.buschmais.jqassistant.plugin.java.api.model.generics;

import com.buschmais.jqassistant.plugin.java.api.model.JavaDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Boundary")
public interface BoundaryDescriptor extends JavaDescriptor {

    @Relation("OF_RAW_TYPE")
    TypeDescriptor getRawType();

    void setRawType(TypeDescriptor rawType);



}
