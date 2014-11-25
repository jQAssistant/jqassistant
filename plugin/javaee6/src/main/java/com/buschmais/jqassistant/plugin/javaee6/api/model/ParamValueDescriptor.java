package com.buschmais.jqassistant.plugin.javaee6.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("ParamValue")
public interface ParamValueDescriptor extends WebDescriptor {

    @Relation("HAS_DESCRIPTION")
    List<DescriptionDescriptor> getDescriptions();

    String getParamName();

    void setParamName(String paramName);

    String getParamValue();

    void setParamValue(String value);
}
