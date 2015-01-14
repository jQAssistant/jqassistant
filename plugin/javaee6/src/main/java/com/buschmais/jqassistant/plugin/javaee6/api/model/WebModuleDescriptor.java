package com.buschmais.jqassistant.plugin.javaee6.api.model;

public interface WebModuleDescriptor extends EnterpriseApplicationModuleDescriptor, WebDescriptor {

    String getContextRoot();

    void setContextRoot(String value);
}
