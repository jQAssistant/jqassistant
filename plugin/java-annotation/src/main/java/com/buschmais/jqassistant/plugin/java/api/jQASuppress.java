package com.buschmais.jqassistant.plugin.java.api;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Indicates that rule ids (constraints or concepts) should be suppressed for
 * the annotated element.
 *
 * @deprecated The package for this annotation has changed, migrate to {@link com.buschmais.jqassistant.plugin.java.annotation.jQASuppress}.
 */
@Deprecated(forRemoval = true)
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

    /**
     * The expiration date for this suppression.
     */
    String until() default "";

}
