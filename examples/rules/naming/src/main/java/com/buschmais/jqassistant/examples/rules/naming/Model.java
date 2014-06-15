package com.buschmais.jqassistant.examples.rules.naming;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation marks a class as model.
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface Model {
}
