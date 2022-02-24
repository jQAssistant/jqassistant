package com.buschmais.jqassistant.core.shared.annotation;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation providing a description text.
 */
@Retention(RUNTIME)
public @interface Description {

    String value();

    boolean deprecated() default false;

}
