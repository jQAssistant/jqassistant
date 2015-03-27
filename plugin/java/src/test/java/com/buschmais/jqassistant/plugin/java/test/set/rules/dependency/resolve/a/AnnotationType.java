package com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.resolve.a;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface AnnotationType {

    int value() default 42;

}
