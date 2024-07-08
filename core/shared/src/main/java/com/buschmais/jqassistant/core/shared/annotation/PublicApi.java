package com.buschmais.jqassistant.core.shared.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a type as part of the public API of jQAssistant.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
@Documented
public @interface PublicApi {
}
