package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Defines a group.
 */
public class Group implements Rule {

    /**
     * The id of the group.
     */
    private String id;

    /**
     * The optional description.
     */
    private String description;

    /**
     * The set of concepts contained in the group.
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
     * @param concepts
     *            The included concepts.
     * @param constraints
     *            The included constraints.
     * @param groups
     *            The included groups.
     */
    public Group(String id, String description, Map<String, Severity> concepts, Map<String, Severity> constraints, Set<String> groups) {
        this.id = id;
        this.description = description;
        this.concepts = concepts;
        this.constraints = constraints;
        this.groups = groups;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Group that = (Group) o;
        if (!id.equals(that.id))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Group{" + "id='" + id + '\'' + ", concepts=" + concepts + ", constraints=" + constraints + ", groups=" + groups + '}';
    }
}
