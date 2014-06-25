package com.buschmais.jqassistant.plugin.cdi.test.set.beans;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;

@SessionScoped
public class SessionScopedBean implements Serializable {

    @Produces
    @SessionScoped
    private String producerField;

    @Produces
    @SessionScoped
    public String producerMethod() {
        return "value";
    }
}
