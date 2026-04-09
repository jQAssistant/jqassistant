package com.buschmais.jqassistant.plugin.java.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("ProvidedService")
public interface ProvidedServiceDescriptor extends JavaDescriptor {

    @Relation("OF_TYPE")
    TypeDescriptor getService();

    void setService(TypeDescriptor service);

    @Relation("WITH_PROVIDER")
    List<TypeDescriptor> getProviders();

}
