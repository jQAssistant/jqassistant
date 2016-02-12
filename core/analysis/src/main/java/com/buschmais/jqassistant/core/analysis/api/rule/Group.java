package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a group.
 */
public class Group extends AbstractSeverityRule {

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
    private Map<String, Severity> groups = new HashMap<>();

    private Group() {
    }

    public Map<String, Severity> getConcepts() {
        return concepts;
    }

    public Map<String, Severity> getConstraints() {
        return constraints;
    }

    public Map<String, Severity> getGroups() {
        return groups;
    }

    public static class Builder extends AbstractSeverityRule.Builder<Group.Builder, Group> {

        protected Builder(Group rule) {
            super(rule);
        }

        public static Builder newGroup() {
            return new Builder(new Group());
        }

        public Builder conceptIds(Map<String, Severity> concepts) {
            get().concepts.putAll(concepts);
            return builder();
        }

        public Builder conceptId(String id, Severity severity) {
            get().concepts.put(id, severity);
            return builder();
        }

        public Builder conceptId(String id) {
            return conceptId(id, null);
        }

        public Builder constraintIds(Map<String, Severity> constraints) {
            get().constraints.putAll(constraints);
            return builder();
        }

        public Builder constraintId(String id, Severity severity) {
            get().constraints.put(id, severity);
            return builder();
        }

        public Builder constraintId(String id) {
            return constraintId(id, null);
        }

        public Builder groupIds(Map<String, Severity> groups) {
            get().groups.putAll(groups);
            return builder();
        }

        public Builder groupId(String id, Severity severity) {
            get().groups.put(id, severity);
            return builder();
        }

        public Builder groupId(String id) {
            return groupId(id, null);
        }
    }

}
