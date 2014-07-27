package com.buschmais.jqassistant.core.analysis.api.rule;

import com.buschmais.jqassistant.core.analysis.rules.schema.v1.SeverityEnumType;

/**
 * Defines a constraint to be validated.
 */
public class Constraint extends AbstractRule {

    /**
     * The severity of the constraint.
     */
    private Severity severity;

    /**
     * Returns the severity of the constraint.
     * 
     * @return {@link Severity}
     */
    public Severity getSeverity() {
        return severity;
    }

    /**
     * Sets the severity of the constraint.
     * 
     * @param severity
     *            severity value
     */
    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    /**
     * Sets the severity of the constraint.
     * 
     * @param severity
     *            severity value
     */
    public void setSeverity(SeverityEnumType severity) {
        if (severity != null) {
            this.severity = Severity.fromValue(severity.value());
        }
    }

}
