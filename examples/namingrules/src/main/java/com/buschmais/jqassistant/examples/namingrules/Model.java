package com.buschmais.jqassistant.examples.namingrules;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation marks a class as model.
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface Model {
}
