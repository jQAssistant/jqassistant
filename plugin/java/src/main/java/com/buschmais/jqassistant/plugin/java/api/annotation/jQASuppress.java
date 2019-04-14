package com.buschmais.jqassistant.plugin.java.api.annotation;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Indicates that the rule ids (constraints or concepts) should be suppressed
 * for the annotated element.
 */
@Retention(CLASS)
public @interface jQASuppress {

    /**
     * The rule ids.
     * 
     * @return The rule ids.
     */
    String[] value();

}
