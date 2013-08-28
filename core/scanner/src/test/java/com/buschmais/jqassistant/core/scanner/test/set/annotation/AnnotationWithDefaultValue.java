package com.buschmais.jqassistant.core.scanner.test.set.annotation;

import java.lang.annotation.*;
import java.lang.annotation.Annotation;

/**
 * Defines an annotation with a value having a default.
 */
public @interface AnnotationWithDefaultValue {

    Class<?> classValue() default Number.class;

    Enumeration enumerationValue() default Enumeration.DEFAULT;

    double primitiveValueValue() default 0;

    Class[] arrayValue() default {Integer.class};

    NestedAnnotation annotationValue() default @NestedAnnotation("test"); // creates an implict dependency to java.lang.String
}
