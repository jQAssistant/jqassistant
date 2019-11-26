package com.buschmais.jqassistant.core.report.api.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.buschmais.xo.api.CompositeObject;

/**
 * A meta-annotation to mark {@link CompositeObject}s as language elements.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Language {
}
