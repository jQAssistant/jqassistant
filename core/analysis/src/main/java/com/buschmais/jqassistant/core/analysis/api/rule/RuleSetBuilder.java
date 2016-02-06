package com.buschmais.jqassistant.core.analysis.api.rule;

import com.buschmais.jqassistant.core.analysis.api.RuleException;

/**
 * A rule set builder.
 */
public class RuleSetBuilder {

    private DefaultRuleSet ruleSet = new DefaultRuleSet();

    /**
     * Private constructor.
     */
    private RuleSetBuilder() {
    }

    public static RuleSetBuilder newInstance() {
        return new RuleSetBuilder();
    }

    public RuleSetBuilder addTemplate(Template template) throws RuleException {
        ruleSet.templateBucket.add(template);
        return this;
    }

    public RuleSetBuilder addConcept(Concept concept) throws RuleHandlingException {
        ruleSet.conceptBucket.add(concept);

        return this;
    }

    public RuleSetBuilder addConstraint(Constraint constraint) throws RuleException {
        ruleSet.constraintBucket.add(constraint);

        return this;
    }

    public RuleSetBuilder addGroup(Group group) throws RuleException {
        ruleSet.groupsBucket.add(group);

        return this;
    }

    public RuleSetBuilder addMetricGroup(MetricGroup metricGroup) throws RuleException {
        ruleSet.getMetricGroupsBucket().add(metricGroup);

        return this;
    }

    public RuleSet getRuleSet() {
        return ruleSet;
    }

    /**
     * Defines a set of rules containing all resolved {@link Concept} s, {@link Constraint}s and {@link Group}s.
     */
    private static class DefaultRuleSet implements RuleSet {

        private ConceptBucket conceptBucket = new ConceptBucket();
        private ConstraintBucket constraintBucket = new ConstraintBucket();
        private TemplateBucket templateBucket = new TemplateBucket();
        private GroupsBucket groupsBucket = new GroupsBucket();
        private MetricGroupsBucket metricGroupsBucket = new MetricGroupsBucket();

        private DefaultRuleSet() {
        }

        @Override
        public TemplateBucket getTemplateBucket() {
            return templateBucket;
        }

        public ConceptBucket getConceptBucket() {
            return conceptBucket;
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
        public String toString() {
            return "RuleSet{" + "groups=" + groupsBucket.size() + ", constraints=" + constraintBucket.size() +
                    ", rules=" + conceptBucket.size() + ", metric groups=" + metricGroupsBucket.size() + "}";
        }
    }
}
