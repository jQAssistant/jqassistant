package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a metric that can be executed.
 */
public class Metric extends AbstractExecutableRule {

    private Map<String, Class<?>> parameterTypes = new HashMap<>();

    private Metric() {
    }

    public Map<String, Class<?>> getParameterTypes() {
        return parameterTypes;
    }

    public static class Builder extends AbstractExecutableRule.Builder<Metric.Builder, Metric> {

        protected Builder(Metric rule) {
            super(rule);
        }

        public static Builder newMetric() {
            return new Builder(new Metric());
        }

        public Builder parameterTypes(Map<String, Class<?>> parameterTypes) {
            get().parameterTypes.putAll(parameterTypes);
            return builder();
        }
    }
}
