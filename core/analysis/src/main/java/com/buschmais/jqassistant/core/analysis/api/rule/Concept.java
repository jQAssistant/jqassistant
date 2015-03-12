package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Map;
import java.util.Set;

/**
 * Defines a concept which can be applied.
 * 
 * Used to add information to the database.
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
     *            The parametes.
     * @param requiresConcepts
     *            The required concept ids.
     * @param verification
     *            The result verification.
     * @param report
     *            The report settings.
     */
    public Concept(String id, String description, Severity severity, String deprecated, String cypher, Script script, String templateId,
            Map<String, Object> parameters, Set<String> requiresConcepts, Verification verification, Report report) {
        super(id, description, severity, deprecated, cypher, script, templateId, parameters, requiresConcepts, verification, report);
    }
}
