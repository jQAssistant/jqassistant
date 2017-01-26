package com.buschmais.jqassistant.core.analysis.api.rule;

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
