package com.buschmais.jqassistant.plugin.maven3.api.model;

import java.util.List;

import com.buschmais.jqassistant.plugin.java.api.model.ValueDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Configuration")
public interface MavenConfigurationDescriptor extends MavenDescriptor {

    @Relation("CONTAINS")
    List<ValueDescriptor<?>> getValues();
}
