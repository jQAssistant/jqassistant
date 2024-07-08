package com.buschmais.jqassistant.core.rule.api.model;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.*;

import static lombok.AccessLevel.PRIVATE;

/**
 * Represents a selection of rules.
 */
@Getter
@Builder
@ToString
@AllArgsConstructor(access = PRIVATE)
public class RuleSelection {

    private static final String GROUP_DEFAULT = "default";

    @Singular
    private Set<String> conceptIds;

    @Singular
    private Set<String> constraintIds;

    @Singular
    private Set<String> excludeConstraintIds;

    @Singular
    private Set<String> groupIds;

    public static RuleSelection select(RuleSet ruleSet, Optional<List<String>> groupIds, Optional<List<String>> constraintIds,
        Optional<List<String>> excludeConstraintIds, Optional<List<String>> conceptIds) {
        RuleSelectionBuilder builder = builder();
        if (groupIds.isEmpty() && conceptIds.isEmpty() && constraintIds.isEmpty() && ruleSet.getGroupsBucket()
            .getIds()
            .contains(GROUP_DEFAULT)) {
            builder.groupIds(Set.of(GROUP_DEFAULT));
        } else {
            // use LinkedHashSet to keep order of selection
            groupIds.ifPresent(groups -> builder.groupIds(new LinkedHashSet<>(groups)));
            constraintIds.ifPresent(constraints -> builder.constraintIds(new LinkedHashSet<>(constraints)));
            conceptIds.ifPresent(concepts -> builder.conceptIds(new LinkedHashSet<>(concepts)));
        }
        excludeConstraintIds.ifPresent(builder::excludeConstraintIds);
        return builder.build();
    }
}
