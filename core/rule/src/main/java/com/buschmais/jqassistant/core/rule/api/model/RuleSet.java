package com.buschmais.jqassistant.core.rule.api.model;

import java.util.Map;
import java.util.Set;

public interface RuleSet {

    ConceptBucket getConceptBucket();

    Map<String, Map<String, Concept.Activation>> getProvidedConcepts();

    Map<String, Set<String>> getProvidingConcepts();

    ConstraintBucket getConstraintBucket();

    GroupsBucket getGroupsBucket();

}
