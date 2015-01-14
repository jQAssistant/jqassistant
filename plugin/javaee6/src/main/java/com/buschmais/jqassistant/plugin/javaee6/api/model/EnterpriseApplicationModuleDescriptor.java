package com.buschmais.jqassistant.plugin.javaee6.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.ApplicationDescriptor;
import com.buschmais.xo.api.annotation.Abstract;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Abstract
@Label("Module")
public interface EnterpriseApplicationModuleDescriptor extends EnterpriseDescriptor, ApplicationDescriptor {

    String getPath();

    void setPath(String value);
}
