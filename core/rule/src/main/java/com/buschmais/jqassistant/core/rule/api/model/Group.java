package com.buschmais.jqassistant.core.rule.api.model;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import lombok.Getter;

/**
 * Defines a group.
 */
@Getter
public class Group extends AbstractSeverityRule {

    public static Severity DEFAULT_SEVERITY = null;

    public static Severity DEFAULT_INCLUDE_SEVERITY = null;

    /**
     * The set of rules contained in the group.
     */
    private final Map<String, Severity> concepts = new LinkedHashMap<>();

    /**
     * The provided concepts, where the key represents the id of the provided concept and the value the set of providing concepts,
     */
    private final Map<String, Set<String>> providedConcepts = new HashMap<>();

    /**
     * The set of constraints contained in the group.
     */
    private final Map<String, Severity> constraints = new LinkedHashMap<>();

    /**
     * The set of groups contained in the group.
     */
    private Map<String, Severity> groups = new LinkedHashMap<>();

    public static class GroupBuilder extends AbstractSeverityRule.Builder<Group.GroupBuilder, Group> {
        public GroupBuilder(Group group) {
            super(group);
        }

        @Override
        protected GroupBuilder getThis() {
            return this;
        }

        public GroupBuilder concepts(Map<String, Severity> concepts) {
            rule.concepts.putAll(concepts);
            return this;
        }

        public GroupBuilder providedConcepts(Map<String, Set<String>> providedConcepts) {
            rule.providedConcepts.putAll(providedConcepts);
            return this;
        }

        public GroupBuilder constraints(Map<String, Severity> constraints) {
            rule.constraints.putAll(constraints);
            return this;
        }

        public GroupBuilder groups(Map<String, Severity> groups) {
            rule.groups.putAll(groups);
            return this;
        }

        public GroupBuilder concept(String id, Severity severity) {
            rule.concepts.put(id, severity);
            return this;
        }

        public GroupBuilder constraint(String id, Severity severity) {
            rule.constraints.put(id, severity);
            return this;
        }

        public GroupBuilder group(String id, Severity severity) {
            rule.groups.put(id, severity);
            return this;
        }
    }

    public static Group.GroupBuilder builder() {
        return new GroupBuilder(new Group());
    }



}
