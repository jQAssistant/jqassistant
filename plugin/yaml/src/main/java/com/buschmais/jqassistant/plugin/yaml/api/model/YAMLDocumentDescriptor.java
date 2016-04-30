package com.buschmais.jqassistant.plugin.yaml.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Document")
public interface YAMLDocumentDescriptor extends YAMLDescriptor, YAMLKeyBucket,
                                                YAMLValueBucket {

    @Override
    @Relation("CONTAINS_KEY")
    List<YAMLKeyDescriptor> getKeys();

    @Override
    @Relation("CONTAINS_VALUE")
    List<YAMLValueDescriptor> getValues();
}
