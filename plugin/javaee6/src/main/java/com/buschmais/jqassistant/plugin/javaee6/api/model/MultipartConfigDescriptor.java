package com.buschmais.jqassistant.plugin.javaee6.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;

import com.sun.java.xml.ns.javaee.String;

@Label("MultipartConfig")
public interface MultipartConfigDescriptor extends WebDescriptor {

    Long getFileSizeThreshold();

    void setFileSizeThreshold(Long fileSizeThreshold);

    String getLocation();

    void setLocation(String location);

    Long getMaxFileSize();

    void setMaxFileSize(Long maxFileSize);

    Long getMaxRequestSize();

    void setMaxRequestSize(Long maxRequestSize);
}
