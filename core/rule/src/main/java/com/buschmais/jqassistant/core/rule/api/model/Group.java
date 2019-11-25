package com.buschmais.jqassistant.core.rule.api.model;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;

/**
 * Defines a group.
 */
@Getter
public class Group extends AbstractSeverityRule {

    /**
     * The set of rules contained in the group.
     */
    private Map<String, Severity> concepts = new LinkedHashMap<>();

    /**
     * The set of constraints contained in the group.
     */
    private Map<String, Severity> constraints = new LinkedHashMap<>();

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

            return getThis();
        }

        public GroupBuilder constraints(Map<String, Severity> constraints) {
            rule.constraints.putAll(constraints);

            return getThis();
        }

        public GroupBuilder groups(Map<String, Severity> groups) {
            rule.groups.putAll(groups);

            return getThis();
        }

        public GroupBuilder concept(String id, Severity severity) {
            rule.concepts.put(id, severity);

            return getThis();
        }

        public GroupBuilder constraint(String id, Severity severity) {
            rule.constraints.put(id, severity);

            return getThis();
        }

        public GroupBuilder group(String id, Severity severity) {
            rule.groups.put(id, severity);

            return getThis();
        }
    }

    public static Group.GroupBuilder builder() {
        return new GroupBuilder(new Group());
    }



}
