package com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation.Enumeration.DEFAULT;

/**
 * An annotation containing values of all supported types.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Annotation {

    String value();

    String[] arrayValue() default {};

    Class<?> classValue() default Object.class;

    Enumeration enumerationValue() default DEFAULT;

    NestedAnnotation nestedAnnotationValue() default @NestedAnnotation("default");

}
