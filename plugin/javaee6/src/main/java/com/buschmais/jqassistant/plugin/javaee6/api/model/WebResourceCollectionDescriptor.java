package com.buschmais.jqassistant.plugin.javaee6.api.model;

import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("ResourceCollection")
public interface WebResourceCollectionDescriptor extends WebDescriptor, NamedDescriptor {

    @Relation("HAS_DESCRIPTION")
    List<DescriptionDescriptor> getDescriptions();

    @Relation("HAS_HTTP_METHOD")
    List<HttpMethodDescriptor> getHttpMethods();

    @Relation("HAS_HTTP_METHOD_OMISSION")
    List<HttpMethodOmissionDescriptor> getHttpMethodOmissions();

    @Relation("ON_URL_PATTERN")
    List<UrlPatternDescriptor> getUrlPatterns();
}
