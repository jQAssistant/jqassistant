package com.buschmais.jqassistant.plugin.java.api.model;

import com.buschmais.xo.neo4j.api.annotation.Property;

public interface IndexTemplate {

    @Property("index")
    int getIndex();

    void setIndex(int index);

}
