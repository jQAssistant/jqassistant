package com.buschmais.jqassistant.rules.java.test.set.dependency.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An annotation with dependencies.
 */
@Retention(RUNTIME)
public @interface Annotation {

    String value();

    Class<?>[] classValues();
}
