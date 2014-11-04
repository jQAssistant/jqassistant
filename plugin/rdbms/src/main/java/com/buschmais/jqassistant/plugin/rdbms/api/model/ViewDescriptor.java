package com.buschmais.jqassistant.plugin.rdbms.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("View")
public interface ViewDescriptor extends TableDescriptor {

    boolean isUpdatable();

    void setUpdatable(boolean updatable);

    String getCheckOption();

    void setCheckOption(String name);

}
