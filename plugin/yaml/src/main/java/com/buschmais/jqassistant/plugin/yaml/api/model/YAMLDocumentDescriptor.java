package com.buschmais.jqassistant.plugin.yaml.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.List;

@Label("YAMLDocument")
public interface YAMLDocumentDescriptor extends YAMLDescriptor, YAMLKeyBucket,
                                                YAMLValueBucket {

    @Override
    @Relation("CONTAINS_KEY")
    List<YAMLKeyDescriptor> getKeys();

    @Override
    @Relation("CONTAINS_VALUE")
    List<YAMLValueDescriptor> getValues();
}
