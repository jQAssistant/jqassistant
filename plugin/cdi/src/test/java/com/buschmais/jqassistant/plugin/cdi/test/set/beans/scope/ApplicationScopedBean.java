package com.buschmais.jqassistant.plugin.cdi.test.set.beans.scope;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class ApplicationScopedBean implements Serializable {

    @Produces
    @ApplicationScoped
    private String producerField;

    @Produces
    @ApplicationScoped
    public String producerMethod() {
        return "value";
    }

}
