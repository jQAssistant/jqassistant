package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.core.analysis.api.rule.source.RuleSource;

/**
 * Defines a group.
 */
public class Group extends AbstractRule {

    /**
     * The set of rules contained in the group.
     */
    private Map<String, Severity> concepts = new HashMap<>();

    /**
     * The set of constraints contained in the group.
     */
    private Map<String, Severity> constraints = new HashMap<>();

    /**
     * The set of groups contained in the group.
     */
    private Set<String> groups = new HashSet<>();

    /**
     * Constructor.
     * 
     * @param id
     *            The id of the group.
     * @param description
     *            The description.
     * @param ruleSource
     *            The rule source.
     * @param concepts
     *            The included rules.
     * @param constraints
     *            The included constraints.
     * @param groups
     *            The included groups.
     */
    public Group(String id, String description, RuleSource ruleSource, Map<String, Severity> concepts, Map<String, Severity> constraints,
            Set<String> groups) {
        super(id, description, ruleSource);
        this.concepts = concepts;
        this.constraints = constraints;
        this.groups = groups;
    }

    public Map<String, Severity> getConcepts() {
        return concepts;
    }

    public Map<String, Severity> getConstraints() {
        return constraints;
    }

    public Set<String> getGroups() {
        return groups;
    }
}
