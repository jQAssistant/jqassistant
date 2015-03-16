package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Map;
import java.util.Set;

/**
 * Defines a constraint to be validated.
 */
public class Constraint extends AbstractExecutableRule {

    /** Default severity level for constraints. */
    public static Severity DEFAULT_SEVERITY = Severity.INFO;

    /**
     * Constructor.
     *
     * @param id
     *            The id.
     * @param description
     *            The human readable description.
     * @param severity
     *            The severity.
     * @param deprecated
     *            The deprecated message.
     * @param executable
     *            The executable.
     * @param parameters
     *            The parameters.
     * @param requiresConcepts
     *            The required concepts.
     * @param verification
     *            The result verification.
     * @param report
     *            The report settings.
     */
    public Constraint(String id, String description, Severity severity, String deprecated, Executable executable, Map<String, Object> parameters,
            Set<String> requiresConcepts, Verification verification, Report report) {
        super(id, description, severity, deprecated, executable, parameters, requiresConcepts, verification, report);
    }
}
