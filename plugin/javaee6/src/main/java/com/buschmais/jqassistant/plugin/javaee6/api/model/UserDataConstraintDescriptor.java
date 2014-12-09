package com.buschmais.jqassistant.plugin.javaee6.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("UserDataConstraint")
public interface UserDataConstraintDescriptor extends WebDescriptor {

    String getTransportGuarantee();

    void setTransportGuarantee(String value);

    @Relation("HS_DESCRIPTION")
    List<DescriptionDescriptor> getDescriptions();
}
