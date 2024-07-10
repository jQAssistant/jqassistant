package com.buschmais.jqassistant.plugin.java.api.annotation;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Indicates that rule ids (constraints or concepts) should be suppressed for
 * the annotated element.
 */
@Retention(CLASS)
public @interface jQASuppress {

    /**
     * The rule ids.
     *
     * @return The rule ids.
     */
    String[] value();

    /**
     * The column to be used to identify the suppressed elements
     *
     * @return The column to be used to identify the suppressed elements
     */
    String column() default "";

    /**
     * The human-readable reason for this suppression.
     */
    String reason() default "";
}
