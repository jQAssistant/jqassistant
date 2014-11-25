package com.buschmais.jqassistant.plugin.javaee6.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("ServletMapping")
public interface ServletMappingDescriptor extends WebDescriptor {

    String getServletName();

    void setServletName(String value);

    @Relation("ON_URL_PATTERN")
    List<UrlPatternDescriptor> getUrlPatterns();
}
