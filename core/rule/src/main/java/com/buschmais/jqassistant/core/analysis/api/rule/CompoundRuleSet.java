package com.buschmais.jqassistant.core.analysis.api.rule;

public class CompoundRuleSet implements RuleSet {

    private ConceptBucket conceptBucket = new ConceptBucket();
    private ConstraintBucket constraintBucket = new ConstraintBucket();
    private GroupsBucket groupsBucket = new GroupsBucket();

    public CompoundRuleSet(RuleSet... ruleSets) throws DuplicateRuleException {
        for (RuleSet ruleSet : ruleSets) {
            conceptBucket.add(ruleSet.getConceptBucket());
            constraintBucket.add(ruleSet.getConstraintBucket());
            groupsBucket.add(ruleSet.getGroupsBucket());
        }
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
    public ConceptBucket getConceptBucket() {
        return conceptBucket;
    }
}
