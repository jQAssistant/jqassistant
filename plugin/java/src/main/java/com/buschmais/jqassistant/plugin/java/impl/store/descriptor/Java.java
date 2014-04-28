package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.buschmais.jqassistant.core.store.api.descriptor.Language;

/**
 * Defines the language elements for "Java".
 */
@Language("Java")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Java {

    JavaLanguageElement value();

    enum JavaLanguageElement {
        Package, Type, Field, Method;
    }
}
