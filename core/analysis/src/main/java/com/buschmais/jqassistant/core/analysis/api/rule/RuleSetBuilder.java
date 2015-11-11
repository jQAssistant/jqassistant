package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.HashMap;
import java.util.Map;

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
        return put(ruleSet.templates, template);
    }

    public RuleSetBuilder addConcept(Concept concept) throws RuleHandlingException {
        ruleSet.conceptBucket.addConcept(concept);

        return this;
    }

    public RuleSetBuilder addConstraint(Constraint constraint) throws RuleException {
        ruleSet.constraintBucket.addConstraint(constraint);

        return this;
    }

    public RuleSetBuilder addGroup(Group group) throws RuleException {
        return put(ruleSet.groups, group);
    }

    public RuleSetBuilder addMetricGroup(MetricGroup metricGroup) throws RuleException {
        return put(ruleSet.metricGroups, metricGroup);
    }

    public RuleSet getRuleSet() {
        return ruleSet;
    }

    private <T extends Rule> RuleSetBuilder put(Map<String, T> rules, T rule) throws RuleException {
        T oldRule = rules.put(rule.getId(), rule);
        if (oldRule != null) {
            throw new RuleException("The id of a rule must be unique: type=" + rule.getClass().getSimpleName() + ", id='" + rule.getId()
                    + "', sources='" + rule.getSource() + "' and '" + oldRule.getSource() + "' .");
        }
        return this;
    }

    /**
     * Defines a set of rules containing all resolved {@link Concept} s, {@link Constraint}s and {@link Group}s.
     */
    private static class DefaultRuleSet implements RuleSet {

        private ConceptBucket conceptBucket = new ConceptBucket();
        private ConstraintBucket constraintBucket = new ConstraintBucket();
        private Map<String, Template> templates = new HashMap<>();
        private Map<String, Group> groups = new HashMap<>();
        private Map<String, MetricGroup> metricGroups = new HashMap<>();

        private DefaultRuleSet() {
        }

        @Override
        public Map<String, Template> getTemplates() {
            return templates;
        }

        public ConceptBucket getConceptBucket() {
            return conceptBucket;
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
        public String toString() {
            return "RuleSet{" + "groups=" + groups + ", constraints=" + constraintBucket.size() + ", rules=" + conceptBucket.size() + '}';
        }
    }
}
