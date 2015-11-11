package com.buschmais.jqassistant.core.analysis.api.rule;

public class CompoundRuleSet implements RuleSet {

    private ConceptBucket conceptBucket = new ConceptBucket();
    private ConstraintBucket constraintBucket = new ConstraintBucket();
    private TemplateBucket templateBucket = new TemplateBucket();
    private GroupsBucket groupsBucket = new GroupsBucket();
    private MetricGroupsBucket metricGroupsBucket = new MetricGroupsBucket();

    public CompoundRuleSet(RuleSet... ruleSets) throws DuplicateRuleException {
        for (RuleSet ruleSet : ruleSets) {
            templateBucket.add(ruleSet.getTemplateBucket());
            conceptBucket.add(ruleSet.getConceptBucket());
            constraintBucket.add(ruleSet.getConstraintBucket());
            groupsBucket.add(ruleSet.getGroupsBucket());
            metricGroupsBucket.add(ruleSet.getMetricGroupsBucket());
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
    public MetricGroupsBucket getMetricGroupsBucket() {
        return metricGroupsBucket;
    }

    @Override
    public ConceptBucket getConceptBucket() {
        return conceptBucket;
    }
}
