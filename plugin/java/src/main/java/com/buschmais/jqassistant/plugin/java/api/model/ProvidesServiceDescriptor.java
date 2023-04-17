package com.buschmais.jqassistant.plugin.java.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("ProvidesService")
public interface ProvidesServiceDescriptor extends JavaDescriptor, Descriptor {


    @Relation("OF_TYPE")
    TypeDescriptor getService();

    void setService(TypeDescriptor service);

    @Relation("WITH_PROVIDER")
    List<TypeDescriptor> getProviders();

}
