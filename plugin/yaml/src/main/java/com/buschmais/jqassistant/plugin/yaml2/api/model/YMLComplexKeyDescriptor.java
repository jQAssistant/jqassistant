package com.buschmais.jqassistant.plugin.yaml2.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Complex")
public interface YMLComplexKeyDescriptor extends YMLKeyDescriptor {
    @Relation("HAS_COMPLEX_KEY")
    YMLDescriptor getKey();

    void setKey(YMLDescriptor key);

}
