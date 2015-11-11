package com.buschmais.jqassistant.core.analysis.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;

/**
 * Represents a selection of rules.
 */
public class RuleSelection {

    private static final String GROUP_DEFAULT = "default";

    private List<String> conceptIds = new ArrayList<>();

    private List<String> constraintIds = new ArrayList<>();

    private List<String> groupIds = new ArrayList<>();

    public List<String> getConceptIds() {
        return conceptIds;
    }

    public List<String> getConstraintIds() {
        return constraintIds;
    }

    public List<String> getGroupIds() {
        return groupIds;
    }

    /**
     * A builder for a rule selection.
     */
    public static class Builder {

        private RuleSelection ruleSelection = new RuleSelection();

        /**
         * Creates a
         * {@link com.buschmais.jqassistant.core.analysis.api.RuleSelection} of
         * all rules contained in the given
         * {@link com.buschmais.jqassistant.core.analysis.api.rule.RuleSet}.
         * 
         * @param ruleSet
         *            The rule set.
         * @return The rule selection.
         */
        public static RuleSelection allOf(RuleSet ruleSet) {
            Set<String> conceptIds = ruleSet.getConceptBucket().getConceptIds();
            Set<String> constraintIds = ruleSet.getConstraintBucket().getConstraintIds();

            return newInstance().addGroupIds(ruleSet.getGroupsBucket().getGroupIds())
                                .addConstraintIds(constraintIds)
                                .addConceptIds(conceptIds)
                                .get();
        }

        /**
         * Create the default rule selection from a
         * {@link com.buschmais.jqassistant.core.analysis.api.rule.RuleSet}.
         * 
         * @param ruleSet
         *            The rule set.
         * @return The rule selection.
         */
        public static RuleSelection newDefault(RuleSet ruleSet) {
            Builder builder = newInstance();
            if (ruleSet.getGroupsBucket().getGroupIds().contains(GROUP_DEFAULT)) {
                builder.addGroupId(GROUP_DEFAULT).get();
            }
            return builder.get();
        }

        /**
         * Selects rules.
         *
         * @param ruleSet
         *            The rule set.
         * @return The rule selection.
         */
        public static RuleSelection select(RuleSet ruleSet, Collection<String> groupIds, Collection<String> constraintIds, Collection<String> conceptIds) {
            Builder builder = newInstance().addGroupIds(groupIds).addConstraintIds(constraintIds).addConceptIds(conceptIds);
            if (builder.isEmpty()) {
                return RuleSelection.Builder.newDefault(ruleSet);
            }
            return builder.get();

        }

        /**
         * Create a new builder.
         * 
         * @return The builder.
         */
        public static Builder newInstance() {
            return new Builder();
        }

        public Builder addConceptId(String id) {
            ruleSelection.conceptIds.add(id);
            return this;
        }

        public Builder addConceptIds(Collection<String> ids) {
            ruleSelection.conceptIds.addAll(ids);
            return this;
        }

        public Builder addConstraintId(String id) {
            ruleSelection.constraintIds.add(id);
            return this;
        }

        public Builder addConstraintIds(Collection<String> ids) {
            ruleSelection.constraintIds.addAll(ids);
            return this;
        }

        public Builder addGroupId(String id) {
            ruleSelection.getGroupIds().add(id);
            return this;
        }

        public Builder addGroupIds(Collection<String> ids) {
            ruleSelection.groupIds.addAll(ids);
            return this;
        }

        public RuleSelection get() {
            return ruleSelection;
        }

        public boolean isEmpty() {
            return ruleSelection.conceptIds.isEmpty() && ruleSelection.constraintIds.isEmpty() && ruleSelection.groupIds.isEmpty();
        }
    }
}
