package com.buschmais.jqassistant.plugin.java.test.set.dependency.annotations;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An annotation with dependencies.
 */
@Retention(RUNTIME)
public @interface Annotation {

    String value();

    Class<?>[] classValues();
}
