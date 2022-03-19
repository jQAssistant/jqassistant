package com.buschmais.jqassistant.core.rule.api.model;

import java.util.*;

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
    private Set<String> conceptIds;

    @Singular
    private Set<String> constraintIds;

    @Singular
    private Set<String> groupIds;

    public static RuleSelection select(RuleSet ruleSet, Optional<List<String>> groupIds, Optional<List<String>> constraintIds,
        Optional<List<String>> conceptIds) {
        return select(ruleSet, groupIds.orElse(Collections.emptyList()), constraintIds.orElse(Collections.emptyList()),
            conceptIds.orElse(Collections.emptyList()));
    }

    private static RuleSelection select(RuleSet ruleSet, List<String> groupIds, List<String> constraintIds, List<String> conceptIds) {
        if (groupIds.isEmpty() && conceptIds.isEmpty() && constraintIds.isEmpty() && ruleSet.getGroupsBucket()
            .getIds()
            .contains(GROUP_DEFAULT)) {
            return builder().groupId(GROUP_DEFAULT)
                .build();
        }
        // use LinkedHashSet to keep order of selection
        return builder().groupIds(new LinkedHashSet<>(groupIds))
            .constraintIds(new LinkedHashSet<>(constraintIds))
            .conceptIds(new LinkedHashSet<>(conceptIds))
            .build();
    }
}
