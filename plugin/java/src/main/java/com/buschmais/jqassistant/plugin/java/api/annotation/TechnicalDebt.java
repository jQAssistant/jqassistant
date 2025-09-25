package com.buschmais.jqassistant.plugin.java.api.annotation;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;

import lombok.RequiredArgsConstructor;

import static com.buschmais.jqassistant.plugin.java.api.annotation.TechnicalDebt.Priority.MEDIUM;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Indicates technical debt related to the annotated element.
 */
@Retention(CLASS)
@Repeatable(TechnicalDebt.Repeatable.class)
public @interface TechnicalDebt {

    /**
     * Description of the technical debt.
     */
    String value();

    Priority priority() default MEDIUM;

    /**
     * id of a linked issue (optional).
     */
    String issue() default "";

    /**
     * Classes which are related to this technical debt.
     */
    Class<?>[] seeAlso() default {};

    @RequiredArgsConstructor
    enum Priority {
        HIGH(0),
        MEDIUM(1),
        LOW(2);

        /**
         * Für Sortierbarkeit in Reports
         */
        private final int value;
    }

    @Retention(CLASS)
    @interface Repeatable {
        TechnicalDebt[] value();
    }
}
