package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Map;
import java.util.Set;

/**
 * Defines the interface for rules which can be executed on the database.
 */
public interface ExecutableRule extends Rule {

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
    Set<String> getRequiresConcepts();

    /**
     * Return the executable.
     * 
     * @return The executable.
     */
    Executable getExecutable();

    /**
     * Return the map of parameters.
     * 
     * @return The parameters.
     */
    Map<String, Object> getParameters();

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
