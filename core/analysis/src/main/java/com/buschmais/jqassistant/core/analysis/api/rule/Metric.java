package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.core.analysis.api.rule.source.RuleSource;

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
     * @param ruleSource
     *            The rule source.
     * @param cypherExecutable
     *            The cypher executable.
     * @param requiresConcepts
     *            The required concepts.
     */
    public Metric(String id, String description, RuleSource ruleSource, CypherExecutable cypherExecutable, Map<String, Class<?>> parameterTypes,
            Set<String> requiresConcepts) {
        super(id, description, ruleSource, null, null, cypherExecutable, null, requiresConcepts, null, null);
        this.parameterTypes = parameterTypes;
    }

    public Map<String, Class<?>> getParameterTypes() {
        return parameterTypes;
    }
}
