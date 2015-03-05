package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Map;

/**
 * Represents a template.
 */
public class Template implements Rule {

    private String id;

    private String description;

    private String cypher;

    private Map<String, Class<?>> parameterTypes;

    public Template(String id, String cypher, String description, Map<String, Class<?>> parameterTypes) {
        this.id = id;
        this.description = description;
        this.cypher = cypher;
        this.parameterTypes = parameterTypes;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public String getCypher() {
        return cypher;
    }

    public Map<String, Class<?>> getParameterTypes() {
        return parameterTypes;
    }
}
