package com.buschmais.jqassistant.plugin.javaee6.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("LoginConfig")
public interface LoginConfigDescriptor extends WebDescriptor {

    String getAuthMethod();

    void setAuthMethod(String authMethod);

    @Relation("HAS_FORM_LOGIN_CONFIG")
    FormLoginConfigDescriptor getFormLoginConfig();

    void setFormLoginConfig(FormLoginConfigDescriptor formLoginConfigDescriptor);

    String getRealmName();

    void setRealmName(String realmName);
}
