package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Map;

public interface RuleSet {

    Map<String, Template> getTemplates();

    ConceptBucket getConceptBucket();

    ConstraintBucket getConstraintBucket();

    Map<String, Group> getGroups();

    Map<String, MetricGroup> getMetricGroups();

}
