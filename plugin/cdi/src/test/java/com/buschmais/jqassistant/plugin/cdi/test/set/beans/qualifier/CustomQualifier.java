package com.buschmais.jqassistant.plugin.cdi.test.set.beans.qualifier;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface CustomQualifier {

    String bindingValue();

    @Nonbinding
    String nonBindingValue();
}
