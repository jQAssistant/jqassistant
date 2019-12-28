package com.buschmais.jqassistant.plugin.yaml2.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

@Label("Scalar")
public interface YMLScalarDescriptor extends YMLDescriptor {

    @Property("value")
    String getValue();

    void setValue(String value);

    @Property("index")
    Integer getIndex();

    void setIndex(Integer index);
}
