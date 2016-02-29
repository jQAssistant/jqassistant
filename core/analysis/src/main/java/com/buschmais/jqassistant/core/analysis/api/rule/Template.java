package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Map;

/**
 * Represents a template.
 */
public class Template extends AbstractExecutableRule {

    private Map<String, Class<?>> parameterTypes;

    public Map<String, Class<?>> getParameterTypes() {
        return parameterTypes;
    }

    public static class Builder extends AbstractExecutableRule.Builder<Template.Builder, Template> {

        protected Builder(Template rule) {
            super(rule);
        }

        public static Builder newTemplate() {
            return new Builder(new Template());
        }

        public Builder parameterTypes(Map<String, Class<?>> parameterTypes) {
            get().parameterTypes = parameterTypes;
            return builder();
        }

    }
}
