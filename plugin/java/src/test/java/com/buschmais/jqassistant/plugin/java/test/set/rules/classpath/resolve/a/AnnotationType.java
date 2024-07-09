package com.buschmais.jqassistant.plugin.java.test.set.rules.classpath.resolve.a;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface AnnotationType {

    Class<?> classValue() default Object.class;

    EnumType enumValue() default EnumType.A;
}
