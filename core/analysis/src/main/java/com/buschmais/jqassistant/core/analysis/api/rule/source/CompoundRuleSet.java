package com.buschmais.jqassistant.core.analysis.api.rule.source;

import java.util.Map;
import java.util.TreeMap;

import com.buschmais.jqassistant.core.analysis.api.rule.*;

public class CompoundRuleSet extends AbstractRuleSet {

    private Map<String, QueryTemplate> templates = new TreeMap<>();
    private Map<String, Concept> concepts = new TreeMap<>();
    private Map<String, Constraint> constraints = new TreeMap<>();
    private Map<String, Group> groups = new TreeMap<>();
    private Map<String, MetricGroup> metricGroups = new TreeMap<>();

    public CompoundRuleSet(RuleSet... ruleSets) {
        for (RuleSet ruleSet : ruleSets) {
            templates.putAll(ruleSet.getQueryTemplates());
            concepts.putAll(ruleSet.getConcepts());
            constraints.putAll(ruleSet.getConstraints());
            groups.putAll(ruleSet.getGroups());
            metricGroups.putAll(ruleSet.getMetricGroups());
        }
    }

    @Override
    public Map<String, QueryTemplate> getQueryTemplates() {
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
}
