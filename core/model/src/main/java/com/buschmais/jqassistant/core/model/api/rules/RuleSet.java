package com.buschmais.jqassistant.core.model.api.rules;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Defines a rules containing all resolved {@link Concept}s, {@link Constraint}s and {@link ConstraintGroup}s.
 */
public class RuleSet {

    private Map<String, Concept> concepts = new TreeMap<String, Concept>();
    private Map<String, Constraint> constraints = new TreeMap<String, Constraint>();
    private Map<String, ConstraintGroup> constraintGroups = new TreeMap<String, ConstraintGroup>();

    public Map<String, Concept> getConcepts() {
        return concepts;
    }

    public Map<String, Constraint> getConstraints() {
        return constraints;
    }

    public Map<String, ConstraintGroup> getConstraintGroups() {
        return constraintGroups;
    }
}
