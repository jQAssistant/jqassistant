package com.buschmais.jqassistant.plugin.javaee6.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("SecurityRole")
public interface SecurityRoleDescriptor extends Descriptor {

    @Relation("HAS_DESCRIPTION")
    List<DescriptionDescriptor> getDescriptions();

    @Relation("HAS_ROLE_NAME")
    RoleNameDescriptor getRoleName();

    void setRoleName(RoleNameDescriptor roleName);
}
