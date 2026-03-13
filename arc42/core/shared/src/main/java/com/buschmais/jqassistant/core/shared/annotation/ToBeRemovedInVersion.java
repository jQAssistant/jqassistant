package com.buschmais.jqassistant.core.shared.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates in which version a deprecated element (e.g. class, method, etc.) will be removed.
 * <p>
 * Every element annotated with {@link Deprecated} should also provide this annotation.
 * TODO: Verify with a jQA rule.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ToBeRemovedInVersion {

    /**
     * Return the major version.
     *
     * @return The major version.
     */
    int major();

    /**
     * Return the minor version.
     *
     * @return The minor version.
     */
    int minor();

}
