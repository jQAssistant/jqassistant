package com.buschmais.jqassistant.plugin.cdi.test.set.beans;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Specializes;

@Specializes
public class SpecializesBean extends Bean {

    @Produces
    @Specializes
    @Override
    public String doSomething() {
        return super.doSomething();
    }
}
