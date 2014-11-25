package com.buschmais.jqassistant.plugin.javaee6.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("FilterMapping")
public interface FilterMappingDescriptor extends WebDescriptor {

    String getFilterName();

    void setFilterName(String value);

    @Relation("ON_URL_PATTERN")
    List<UrlPatternDescriptor> getUrlPatterns();
}
