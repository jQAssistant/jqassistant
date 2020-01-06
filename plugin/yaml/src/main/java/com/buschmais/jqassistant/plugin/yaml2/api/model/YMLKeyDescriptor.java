package com.buschmais.jqassistant.plugin.yaml2.api.model;

import com.buschmais.xo.api.annotation.Abstract;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Abstract
@Label("Key")
public interface YMLKeyDescriptor extends YMLDescriptor {
    @Relation("HAS_VALUE")
    YMLDescriptor getValue();

    void setValue(YMLDescriptor descriptor);
}
