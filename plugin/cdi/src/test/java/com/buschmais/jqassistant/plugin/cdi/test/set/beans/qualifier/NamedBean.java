package com.buschmais.jqassistant.plugin.cdi.test.set.beans.qualifier;

import javax.inject.Named;

@Named
public class NamedBean {

    @Named
    public String getValue() {
        return "value";
    }
}
