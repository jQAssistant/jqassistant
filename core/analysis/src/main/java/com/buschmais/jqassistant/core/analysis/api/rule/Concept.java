package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.core.analysis.api.rule.source.RuleSource;

/**
 * Defines a concept which can be applied.
 */
public class Concept extends AbstractExecutableRule {

    /** Default severity level for concept. */
    public static Severity DEFAULT_SEVERITY = Severity.MINOR;

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
     *            The parametes.
     * @param requiresConcepts
     *            The required concept ids.
     * @param verification
     *            The result verification.
     * @param report
     *            The report settings.
     */
    public Concept(String id, String description, RuleSource ruleSource, Severity severity,
                   String deprecated, Executable executable, Map<String, Object> parameters,
                   Set<String> requiresConcepts, Verification verification, Report report) {
        super(id, description, ruleSource, severity, deprecated, executable, parameters,
              requiresConcepts, verification, report);
    }
}
