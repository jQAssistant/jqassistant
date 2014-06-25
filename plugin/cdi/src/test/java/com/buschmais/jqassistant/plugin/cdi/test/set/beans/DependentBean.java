package com.buschmais.jqassistant.plugin.cdi.test.set.beans;

import java.io.Serializable;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

@Dependent
public class DependentBean implements Serializable {

    @Produces
    @Dependent
    private String producerField;

    @Produces
    @Dependent
    public String producerMethod() {
        return "value";
    }
}
