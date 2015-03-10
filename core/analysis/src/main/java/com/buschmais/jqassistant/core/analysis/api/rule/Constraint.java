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
     * @param cypher
     *            The cypher query.
     * @param script
     *            The script.
     * @param templateId
     *            The query template.
     * @param parameters
     *            The parameters.
     * @param requiresConcepts
     *            The required concepts.
     * @param verification
     *            The result verification.
     */
    public Constraint(String id, String description, Severity severity, String deprecated, String cypher, Script script, String templateId,
            Map<String, Object> parameters, Set<String> requiresConcepts, Verification verification) {
        super(id, description, severity, deprecated, cypher, script, templateId, parameters, requiresConcepts, verification);
    }
}
