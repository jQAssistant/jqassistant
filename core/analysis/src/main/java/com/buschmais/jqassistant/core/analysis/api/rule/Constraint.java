package com.buschmais.jqassistant.core.analysis.api.rule;

/**
 * Defines a constraint to be validated.
 */
public class Constraint extends AbstractRule {

    /** Default severity level. */
    public static Severity DEFAULT_SEVERITY = Severity.INFO;

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

}
