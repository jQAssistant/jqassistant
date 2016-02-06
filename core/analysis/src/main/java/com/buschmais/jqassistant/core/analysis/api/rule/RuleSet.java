package com.buschmais.jqassistant.core.analysis.api.rule;

public interface RuleSet {

    TemplateBucket getTemplateBucket();

    ConceptBucket getConceptBucket();

    ConstraintBucket getConstraintBucket();

    GroupsBucket getGroupsBucket();

    MetricGroupsBucket getMetricGroupsBucket();

}
