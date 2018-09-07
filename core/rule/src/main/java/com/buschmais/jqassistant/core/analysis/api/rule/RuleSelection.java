package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.buschmais.jqassistant.core.shared.annotation.ToBeRemovedInVersion;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Represents a selection of rules.
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RuleSelection {

    private static final String GROUP_DEFAULT = "default";

    private List<String> conceptIds = new ArrayList<>();

    private List<String> constraintIds = new ArrayList<>();

    private List<String> groupIds = new ArrayList<>();

    @Deprecated
    @ToBeRemovedInVersion(major = 1, minor = 6)
    public static Builder builder() {
        return new Builder();
    }

    @Deprecated
    @ToBeRemovedInVersion(major = 1, minor = 6)
    private RuleSelection() {
    }

    /**
     * A builder for a rule selection.
     */
    @Deprecated
    @ToBeRemovedInVersion(major = 1, minor = 6)
    public static class Builder {

        private RuleSelection ruleSelection = new RuleSelection();

        /**
         * Creates a
         * {@link RuleSelection} of
         * all rules contained in the given
         * {@link RuleSet}.
         *
         * @param ruleSet
         *            The rule set.
         * @return The rule selection.
         */
        public static RuleSelection allOf(RuleSet ruleSet) {
            Set<String> conceptIds = ruleSet.getConceptBucket().getIds();
            Set<String> constraintIds = ruleSet.getConstraintBucket().getIds();
            Set<String> groupIds = ruleSet.getGroupsBucket().getIds();

            return builder().addGroupIds(groupIds)
                                .addConstraintIds(constraintIds)
                                .addConceptIds(conceptIds)
                                .build();
        }

        /**
         * Create the default rule selection from a
         * {@link RuleSet}.
         *
         * @param ruleSet
         *            The rule set.
         * @return The rule selection.
         */
        public static RuleSelection newDefault(RuleSet ruleSet) {
            Builder builder = builder();
            if (ruleSet.getGroupsBucket().getIds().contains(GROUP_DEFAULT)) {
                builder.addGroupId(GROUP_DEFAULT);
            }
            return builder.build();
        }

        /**
         * Selects rules.
         *
         * @param ruleSet
         *            The rule set.
         * @return The rule selection.
         */
        public static RuleSelection select(RuleSet ruleSet, Collection<String> groupIds, Collection<String> constraintIds, Collection<String> conceptIds) {
            Builder builder = builder().addGroupIds(groupIds).addConstraintIds(constraintIds).addConceptIds(conceptIds);
            if (builder.isEmpty()) {
                return RuleSelection.Builder.newDefault(ruleSet);
            }
            return builder.build();

        }

        /**
         * Create a new builder.
         *
         * @return The builder.
         */
        @Deprecated
        @ToBeRemovedInVersion(major = 1, minor = 6)
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

        @Deprecated
        @ToBeRemovedInVersion(major = 1, minor = 6)
        public RuleSelection get() {
            return build();
        }

        public RuleSelection build() {
            return ruleSelection;
        }

        public boolean isEmpty() {
            return ruleSelection.conceptIds.isEmpty() && ruleSelection.constraintIds.isEmpty() && ruleSelection.groupIds.isEmpty();
        }
    }
}
