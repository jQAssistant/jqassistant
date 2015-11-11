package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Map;
import java.util.TreeMap;

public class CompoundRuleSet implements RuleSet {

    private ConceptBucket conceptBucket = new ConceptBucket();
    private ConstraintBucket constraintBucket = new ConstraintBucket();
    private TemplateBucket templateBucket = new TemplateBucket();
    private GroupsBucket groupsBucket = new GroupsBucket();
    private Map<String, MetricGroup> metricGroups = new TreeMap<>();

    public CompoundRuleSet(RuleSet... ruleSets) throws DuplicateRuleException {
        for (RuleSet ruleSet : ruleSets) {
            templateBucket.addTemplates(ruleSet.getTemplateBucket());
            conceptBucket.addConcepts(ruleSet.getConceptBucket());
            constraintBucket.addConstraints(ruleSet.getConstraintBucket());
            groupsBucket.addGroups(ruleSet.getGroupsBucket());
            metricGroups.putAll(ruleSet.getMetricGroups());
        }
    }

    @Override
    public TemplateBucket getTemplateBucket() {
        return templateBucket;
    }

    @Override
    public ConstraintBucket getConstraintBucket() {
        return constraintBucket;
    }

    @Override
    public GroupsBucket getGroupsBucket() {
        return groupsBucket;
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
