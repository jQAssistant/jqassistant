package com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.types;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation on field level.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.LOCAL_VARIABLE)
public @interface LocalVariableAnnotation {

    Class<LocalVariableAnnotationValueType> value();

}
