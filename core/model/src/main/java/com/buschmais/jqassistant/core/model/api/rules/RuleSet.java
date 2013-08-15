package com.buschmais.jqassistant.core.model.api.rules;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Defines a rules containing all resolved {@link Concept}s, {@link Constraint}s and {@link Group}s.
 */
public class RuleSet {

    private Map<String, Concept> concepts = new TreeMap<>();
    private Map<String, Constraint> constraints = new TreeMap<>();
    private Map<String, Group> groups = new TreeMap<>();

    private Set<String> missingConcepts = new TreeSet<>();
    private Set<String> missingConstraints = new TreeSet<>();
    private Set<String> missingGroups = new TreeSet<>();

    public Map<String, Concept> getConcepts() {
        return concepts;
    }

    public Map<String, Constraint> getConstraints() {
        return constraints;
    }

    public Map<String, Group> getGroups() {
        return groups;
    }

    public Set<String> getMissingConcepts() {
        return missingConcepts;
    }

    public Set<String> getMissingConstraints() {
        return missingConstraints;
    }

    public Set<String> getMissingGroups() {
        return missingGroups;
    }

}
