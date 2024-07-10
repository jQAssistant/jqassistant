package com.buschmais.jqassistant.core.rule.api.model;

/**
 * Represents a rule with a severity.
 */
public interface SeverityRule extends Rule {

    /**
     * Return the severity.
     *
     * @return The severity.
     */
    Severity getSeverity();

}
