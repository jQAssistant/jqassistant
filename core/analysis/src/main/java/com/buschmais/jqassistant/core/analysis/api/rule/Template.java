package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.rule.source.RuleSource;

/**
 * Represents a template.
 */
public class Template extends AbstractRule {

    private Executable executable;

    private Map<String, Class<?>> parameterTypes;

    public Template(String id, String description, RuleSource ruleSource, Executable executable, Map<String, Class<?>> parameterTypes) {
        super(id, description, ruleSource);
        this.executable = executable;
        this.parameterTypes = parameterTypes;
    }

    public Executable getExecutable() {
        return executable;
    }

    public Map<String, Class<?>> getParameterTypes() {
        return parameterTypes;
    }
}
