package com.buschmais.jqassistant.core.report.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A meta-annotation to mark {@link com.buschmais.jqassistant.core.store.api.descriptor.Descriptor} as language elements.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Language {
}
