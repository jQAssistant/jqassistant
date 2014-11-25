package com.buschmais.jqassistant.plugin.javaee6.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.XmlDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

public interface WebXmlDescriptor extends WebDescriptor, XmlDescriptor, FileDescriptor {

    String getVersion();

    void setVersion(String attributeValue);

    String getDisplayName();

    void setDisplayName(String elementText);

    String getDescription();

    void setDescription(String elementText);

    @Relation("HAS_SESSION_CONFIG")
    SessionConfigDescriptor getSessionConfig();

    void setSessionConfig(SessionConfigDescriptor sessionConfig);

    @Relation("HAS_SERVLET")
    List<ServletDescriptor> getServlets();

    @Relation("HAS_SERVLET_MAPPING")
    List<ServletMappingDescriptor> getServletMappings();

    @Relation("HAS_FILTER")
    List<FilterDescriptor> getFilters();

    @Relation("HAS_FILTER_MAPPING")
    List<FilterMappingDescriptor> getFilterMappings();
}
