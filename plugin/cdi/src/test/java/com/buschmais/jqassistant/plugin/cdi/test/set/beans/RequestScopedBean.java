package com.buschmais.jqassistant.plugin.cdi.test.set.beans;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;

@RequestScoped
public class RequestScopedBean {

    @Produces
    @RequestScoped
    private String producerField;

    @Produces
    @RequestScoped
    public String producerMethod() {
        return "value";
    }
}
