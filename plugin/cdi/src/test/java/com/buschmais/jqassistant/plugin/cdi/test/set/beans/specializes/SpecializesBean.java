package com.buschmais.jqassistant.plugin.cdi.test.set.beans.specializes;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Specializes;

import com.buschmais.jqassistant.plugin.cdi.test.set.beans.Bean;

@Specializes
public class SpecializesBean extends Bean {

    @Produces
    @Specializes
    @Override
    public String doSomething() {
        return super.doSomething();
    }
}
