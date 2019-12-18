package com.buschmais.jqassistant.plugin.yaml2.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Sequence")
public interface YMLSequenceDescriptor extends YMLDescriptor {

    @Property("index")
    Integer getIndex();

    void setIndex(Integer index);

    @Relation("HAS_ITEM")
    List<YMLSequenceDescriptor> getSequences();

    @Relation("HAS_ITEM")
    List<YMLMapDescriptor> getMaps();

    @Relation("HAS_ITEM")
    List<YMLScalarDescriptor> getScalars();
}
