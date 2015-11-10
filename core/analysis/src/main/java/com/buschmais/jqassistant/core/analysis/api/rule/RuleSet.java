package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Map;

public interface RuleSet {

    Map<String, Template> getTemplates();

    ConceptBucket getConceptBucket();

//    Map<String, Concept> getConcepts();

    Map<String, Constraint> getConstraints();

    Map<String, Group> getGroups();

    Map<String, MetricGroup> getMetricGroups();

}
