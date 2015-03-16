package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Map;

/**
 * Represents a template.
 */
public class Template implements Rule {

    private String id;

    private String description;

    private Executable executable;

    private Map<String, Class<?>> parameterTypes;

    public Template(String id, String description, Executable executable, Map<String, Class<?>> parameterTypes) {
        this.id = id;
        this.description = description;
        this.executable = executable;
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

    public Executable getExecutable() {
        return executable;
    }

    public Map<String, Class<?>> getParameterTypes() {
        return parameterTypes;
    }
}
