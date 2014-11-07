package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Map;

/**
 * Represents a query template
 */
public class QueryTemplate {

    private String cypher;

    private Map<String, Class<?>> parameterTypes;

    public QueryTemplate(String cypher, Map<String, Class<?>> parameterTypes) {
        this.cypher = cypher;
        this.parameterTypes = parameterTypes;
    }

    public String getCypher() {
        return cypher;
    }

    public Map<String, Class<?>> getParameterTypes() {
        return parameterTypes;
    }
}
