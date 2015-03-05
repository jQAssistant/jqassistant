package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a set of rules containing all resolved {@link Concept} s,
 * {@link Constraint}s and {@link Group}s.
 */
public class DefaultRuleSet implements RuleSet {

    private Map<String, Template> templates = new HashMap<>();
    private Map<String, Concept> concepts = new HashMap<>();
    private Map<String, Constraint> constraints = new HashMap<>();
    private Map<String, Group> groups = new HashMap<>();
    private Map<String, MetricGroup> metricGroups = new HashMap<>();

    private DefaultRuleSet() {
    }

    @Override
    public Map<String, Template> getTemplates() {
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

    /**
     * A rule set builder.
     */
    public static class Builder {

        private DefaultRuleSet ruleSet = new DefaultRuleSet();

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder addTemplate(String id, Template template) {
            ruleSet.templates.put(id, template);
            return this;
        }

        public Builder addConcept(String id, Concept concept) {
            ruleSet.concepts.put(id, concept);
            return this;
        }

        public Builder addConstraint(String id, Constraint constraint) {
            ruleSet.constraints.put(id, constraint);
            return this;
        }

        public Builder addGroup(String id, Group group) {
            ruleSet.groups.put(id, group);
            return this;
        }

        public Builder addMetricGroup(String id, MetricGroup metricGroup) {
            ruleSet.metricGroups.put(id, metricGroup);
            return this;
        }

        public RuleSet getRuleSet() {
            return ruleSet;
        }
    }

}
