package com.buschmais.jqassistant.core.rule.api.model;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import lombok.*;

/**
 * Represents a selection of rules.
 */
@Getter
@Builder
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RuleSelection {

    private static final String GROUP_DEFAULT = "default";

    @Singular
    private Set<String> conceptIds = new LinkedHashSet<>();

    @Singular
    private Set<String> constraintIds = new LinkedHashSet<>();

    @Singular
    private Set<String> groupIds = new LinkedHashSet<>();

    public static RuleSelection select(RuleSet ruleSet, List<String> groupIds, List<String> constraintIds, List<String> conceptIds) {
        if (groupIds.isEmpty() && conceptIds.isEmpty() && constraintIds.isEmpty() && ruleSet.getGroupsBucket().getIds().contains(GROUP_DEFAULT)) {
            return builder().groupId(GROUP_DEFAULT).build();
        }
        return builder().groupIds(new LinkedHashSet<>(groupIds)).constraintIds(new LinkedHashSet<>(constraintIds)).conceptIds(new LinkedHashSet<>(conceptIds)).build();
    }
}
