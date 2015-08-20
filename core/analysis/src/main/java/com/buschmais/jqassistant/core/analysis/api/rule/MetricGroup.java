package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.rule.source.RuleSource;

/**
 * Defines a group of {@link Metric}s.
 */
public class MetricGroup extends AbstractRule {

    /**
     * The list of metrics.
     */
    private Map<String, Metric> metrics;

    /**
     * Constructor.
     * 
     * @param id
     *            The metric id
     * @param description
     *            The description.
     * @param ruleSource
     *            The rule source.
     * @param metrics
     *            The metrics.
     */
    public MetricGroup(String id, String description, RuleSource ruleSource, Map<String, Metric> metrics) {
        super(id, description, ruleSource);
        this.metrics = metrics;
    }

    public Map<String, Metric> getMetrics() {
        return metrics;
    }
}
