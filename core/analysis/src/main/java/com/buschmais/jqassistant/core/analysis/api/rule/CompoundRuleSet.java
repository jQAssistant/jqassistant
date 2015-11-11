package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Map;
import java.util.TreeMap;

public class CompoundRuleSet implements RuleSet {

    private ConceptBucket conceptBucket = new ConceptBucket();
    private ConstraintBucket constraintBucket = new ConstraintBucket();
    private Map<String, Template> templates = new TreeMap<>();
    private Map<String, Group> groups = new TreeMap<>();
    private Map<String, MetricGroup> metricGroups = new TreeMap<>();

    public CompoundRuleSet(RuleSet... ruleSets) throws DuplicateRuleException {
        for (RuleSet ruleSet : ruleSets) {
            templates.putAll(ruleSet.getTemplates());
            conceptBucket.addConcepts(ruleSet.getConceptBucket());
            constraintBucket.addConstraints(ruleSet.getConstraintBucket());
            groups.putAll(ruleSet.getGroups());
            metricGroups.putAll(ruleSet.getMetricGroups());
        }
    }

    @Override
    public Map<String, Template> getTemplates() {
        return templates;
    }

    @Override
    public ConstraintBucket getConstraintBucket() {
        return constraintBucket;
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
