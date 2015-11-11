package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Map;

public interface RuleSet {

    TemplateBucket getTemplateBucket();

    ConceptBucket getConceptBucket();

    ConstraintBucket getConstraintBucket();

    GroupsBucket getGroupsBucket();

    Map<String, MetricGroup> getMetricGroups();

}
