package com.buschmais.jqassistant.plugin.javaee6.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("FormLoginConfig")
public interface FormLoginConfigDescriptor extends WebDescriptor {

    String getFormLoginPage();

    void setFormLoginPage(String value);

    String getFormErrorPage();

    void setFormErrorPage(String value);
}
