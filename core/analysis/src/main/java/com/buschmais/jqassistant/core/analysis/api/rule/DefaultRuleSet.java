package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Map;

/**
 * Defines a set of rules containing all resolved {@link Concept} s,
 * {@link Constraint}s and {@link Group}s.
 */
public class DefaultRuleSet extends AbstractRuleSet {

    private Map<String, Template> templates;
    private Map<String, Concept> concepts;
    private Map<String, Constraint> constraints;
    private Map<String, Group> groups;
    private Map<String, MetricGroup> metricGroups;

    /**
     * Constructor.
     *
     * @param concepts
     *            The concepts
     * @param constraints
     *            The constraints.
     * @param groups
     *            The groups.
     * @param metricGroups
     *            The metric groups.
     */
    public DefaultRuleSet(Map<String, Template> templates, Map<String, Concept> concepts, Map<String, Constraint> constraints, Map<String, Group> groups,
            Map<String, MetricGroup> metricGroups) {
        this.templates = templates;
        this.concepts = concepts;
        this.constraints = constraints;
        this.groups = groups;
        this.metricGroups = metricGroups;
    }

    @Override
    public Map<String, Template> getQueryTemplates() {
        return templates;
    }

    @Override
    public Map<String, Concept> getConcepts() {
        return concepts;
    }

    @Override
    public Map<String, Constraint> getConstraints() {
        return constraints;
    }

    @Override
    public Map<String, Group> getGroups() {
        return groups;
    }

    @Override
    public Map<String, MetricGroup> getMetricGroups() {
        return metricGroups;
    }

    @Override
    public String toString() {
        return "RuleSet{" + "groups=" + groups + ", constraints=" + constraints + ", concepts=" + concepts + '}';
    }

}
