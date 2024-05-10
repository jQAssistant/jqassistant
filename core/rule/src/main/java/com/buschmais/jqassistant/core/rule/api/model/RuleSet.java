package com.buschmais.jqassistant.core.rule.api.model;

import java.util.Map;
import java.util.Set;

public interface RuleSet {

    ConceptBucket getConceptBucket();

    Map<String, Set<String>> getProvidedConcepts();

    Map<String, Set<String>> getProvidingConcepts();

    ConstraintBucket getConstraintBucket();

    GroupsBucket getGroupsBucket();

}
