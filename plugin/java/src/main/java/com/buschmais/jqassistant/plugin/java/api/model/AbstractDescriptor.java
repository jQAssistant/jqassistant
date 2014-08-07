package com.buschmais.jqassistant.plugin.java.api.model;

import com.buschmais.xo.neo4j.api.annotation.Property;

public interface AbstractDescriptor {

    @Property("abstract")
    Boolean isAbstract();

    void setAbstract(Boolean isAbstract);

}
