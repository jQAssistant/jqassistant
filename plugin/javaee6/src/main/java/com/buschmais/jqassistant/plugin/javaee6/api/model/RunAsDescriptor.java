package com.buschmais.jqassistant.plugin.javaee6.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("RunAs")
public interface RunAsDescriptor extends WebDescriptor {

    String getRoleName();

    void setRoleName(String roleName);

    @Relation("HAS_DESCRIPTION")
    List<DescriptionDescriptor> getDescriptions();
}
