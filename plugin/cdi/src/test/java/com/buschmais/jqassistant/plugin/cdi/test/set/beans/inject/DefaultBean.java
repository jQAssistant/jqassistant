package com.buschmais.jqassistant.plugin.cdi.test.set.beans.inject;

import javax.enterprise.inject.Default;
import javax.inject.Inject;

import com.buschmais.jqassistant.plugin.cdi.test.set.beans.Bean;

public class DefaultBean {

    @Inject
    @Default
    private Bean bean;

}
