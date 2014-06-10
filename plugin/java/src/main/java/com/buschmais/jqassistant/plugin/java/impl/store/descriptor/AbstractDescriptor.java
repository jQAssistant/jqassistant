package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.xo.neo4j.api.annotation.Property;

public interface AbstractDescriptor {

    @Property("abstract")
    Boolean isAbstract();

    void setAbstract(Boolean isAbstract);

}
