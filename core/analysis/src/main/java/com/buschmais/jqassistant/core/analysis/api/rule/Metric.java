package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Map;
import java.util.Set;

/**
 * Defines a metric that can be executed.
 */
public class Metric extends AbstractExecutableRule {

    private Map<String, Class<?>> parameterTypes;

    /**
     * Constructor.
     *
     * @param id
     *            The id.
     * @param description
     *            The human readable description.
     * @param cypherExecutable
     *            The cypher executable.
     * @param requiresConcepts
     *            The required concepts.
     */
    public Metric(String id, String description, CypherExecutable cypherExecutable, Map<String, Class<?>> parameterTypes, Set<String> requiresConcepts) {
        super(id, description, null, null, cypherExecutable, null, requiresConcepts, null, null);
        this.parameterTypes = parameterTypes;
    }

    public Map<String, Class<?>> getParameterTypes() {
        return parameterTypes;
    }
}
