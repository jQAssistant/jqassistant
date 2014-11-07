package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Map;

/**
 * Defines a group of {@link Metric}s.
 */
public class MetricGroup {

    /**
     * The list of metrics.
     */
    private Map<String, Metric> metrics;
    /**
     * The id of the rule.
     */
    private String id;
    /**
     * The optional description.
     */
    private String description;

    /**
     * Constructor.
     * 
     * @param id
     *            The metric id
     * @param description
     *            The description.
     * @param metrics
     *            The metrics.
     */
    public MetricGroup(String id, String description, Map<String, Metric> metrics) {
        this.id = id;
        this.description = description;
        this.metrics = metrics;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Metric> getMetrics() {
        return metrics;
    }
}
