package com.buschmais.jqassistant.core.analysis.api.rule;

public interface RuleSet {

    ConceptBucket getConceptBucket();

    ConstraintBucket getConstraintBucket();

    GroupsBucket getGroupsBucket();

}