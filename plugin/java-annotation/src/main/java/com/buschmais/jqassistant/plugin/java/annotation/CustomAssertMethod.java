package com.buschmais.jqassistant.plugin.java.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Indicates that the annotated method represents a custom assert method for tests.
 */
@Retention(CLASS)
@Target(ElementType.METHOD)
public @interface CustomAssertMethod {

}
