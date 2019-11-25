package com.buschmais.jqassistant.core.rule.api.model;

import java.util.Map;

/**
 * Defines the interface for rules which can be executed on the database.
 */
public interface ExecutableRule<E extends Executable> extends Rule {

    /*
     * Return the severity.
     *
     * @return The severity.
     */
    Severity getSeverity();

    /**
     * Return the ids of required rules.
     *
     * @return The ids of required rules.
     */
    Map<String, Boolean> getRequiresConcepts();

    /**
     * Return the executable.
     *
     * @return The executable.
     */
    E getExecutable();

    /**
     * Return the map of required parameters.
     *
     * @return The parameters.
     */
    Map<String, Parameter> getParameters();

    /**
     * Return the result verification for this rule.
     *
     * @return The result verification.
     */
    Verification getVerification();

    /**
     * Return the report settings for this rule.
     *
     * @return The report settings.
     */
    Report getReport();
}
