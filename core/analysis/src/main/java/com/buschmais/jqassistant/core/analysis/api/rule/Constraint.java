package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.core.analysis.api.rule.source.RuleSource;

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
     * @param ruleSource
     *            The rule source.
     * @param severity
     *            The severity.
     * @param deprecated
     *            The deprecated message.
     * @param executable
     *            The executable.
     * @param parameters
     *            The parameters.
     * @param requiresConcepts
     *            The required rules.
     * @param verification
     *            The result verification.
     * @param report
     *            The report settings.
     */
    public Constraint(String id, String description, RuleSource ruleSource, Severity severity, String deprecated, Executable executable,
            Map<String, Object> parameters, Set<String> requiresConcepts, Verification verification, Report report) {
        super(id, description, ruleSource, severity, deprecated, executable, parameters, requiresConcepts, verification, report);
    }
}
