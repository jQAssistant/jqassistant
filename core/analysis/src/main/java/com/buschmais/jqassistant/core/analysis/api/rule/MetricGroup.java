package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Defines a group of {@link Metric}s.
 */
public class MetricGroup {

    /**
     * The list of metrics.
     */
    private final Map<String, Metric> metrics = new TreeMap<>();
    /**
     * The id of the rule.
     */
    private String id;
    /**
     * The optional description.
     */
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Add a metric to this group.
     * 
     * @param metric
     *            the metric to add
     */
    public void addMetric(Metric metric) {

        if (metric == null) {
            return;
        }

        metrics.put(metric.getId(), metric);
    }

    /**
     * Get the list of metrics of this group.
     * 
     * @return the list of metrics, never {@code null}
     */
    public Map<String, Metric> getMetrics() {

        return Collections.unmodifiableMap(metrics);
    }
}
