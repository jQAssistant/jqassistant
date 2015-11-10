package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Map;
import java.util.TreeMap;

public class CompoundRuleSet implements RuleSet {

    private ConceptBucket conceptBucket = new ConceptBucket();
    private Map<String, Template> templates = new TreeMap<>();
    private ConceptBucket concepts = new ConceptBucket();
    private Map<String, Constraint> constraints = new TreeMap<>();
    private Map<String, Group> groups = new TreeMap<>();
    private Map<String, MetricGroup> metricGroups = new TreeMap<>();

    public CompoundRuleSet(RuleSet... ruleSets) throws DuplicateConceptException {
        for (RuleSet ruleSet : ruleSets) {
            templates.putAll(ruleSet.getTemplates());
            concepts.addConcepts(ruleSet.getConceptBucket());
            constraints.putAll(ruleSet.getConstraints());
            groups.putAll(ruleSet.getGroups());
            metricGroups.putAll(ruleSet.getMetricGroups());
        }
    }

    @Override
    public Map<String, Template> getTemplates() {
        return templates;
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
    public ConceptBucket getConceptBucket() {
        return conceptBucket;
    }
}
