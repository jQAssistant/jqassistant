package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a group of {@link Metric}s.
 */
public class MetricGroup extends AbstractRule {

    /**
     * The list of metrics.
     */
    private Map<String, Metric> metrics = new HashMap<>();

    private MetricGroup() {
    }

    public Map<String, Metric> getMetrics() {
        return metrics;
    }

    public static class Builder extends AbstractRule.Builder<MetricGroup.Builder, MetricGroup> {

        protected Builder(MetricGroup rule) {
            super(rule);
        }

        public static Builder newMetricGroup() {
            return new Builder(new MetricGroup());
        }

        public Builder metrics(Map<String, Metric> metrics) {
            get().metrics.putAll(metrics);
            return builder();
        }
    }
}
