package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Map;
import java.util.Set;

/**
 * Defines a constraint to be validated.
 */
public class Constraint extends AbstractRule {

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
     * @param queryTemplateId
     *            The query template.
     * @param parameters
     *            The parametes.
     * @param requiresConcepts
     */
    public Constraint(String id, String description, Severity severity, String deprecated, String cypher, String queryTemplateId,
            Map<String, Object> parameters, Set<String> requiresConcepts) {
        super(id, description, severity, deprecated, cypher, queryTemplateId, parameters, requiresConcepts);
    }
}
