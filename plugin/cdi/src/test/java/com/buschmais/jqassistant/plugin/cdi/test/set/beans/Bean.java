package com.buschmais.jqassistant.plugin.cdi.test.set.beans;

import javax.enterprise.inject.Produces;

public class Bean {

    @Produces
    public String doSomething() {
        return "value";
    }
}
