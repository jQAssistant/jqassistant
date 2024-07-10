package com.buschmais.jqassistant.core.analysis.api.baseline;

import java.util.SortedMap;
import java.util.TreeMap;

import lombok.Getter;
import lombok.ToString;

/**
 * Represents a baseline for analyze results.
 * <p>
 * Note that the data structures rely on {@link SortedMap}s to preserve the order of entries. This is required for creating diffs between baseline files which are generated from these structures.
 */
@Getter
@ToString
public class Baseline {

    /**
     * The baseline per {@link com.buschmais.jqassistant.core.rule.api.model.Concept} id.
     */
    private SortedMap<String, RuleBaseline> concepts = new TreeMap<>();

    /**
     * The baseline per {@link com.buschmais.jqassistant.core.rule.api.model.Constraint} id.
     */
    private SortedMap<String, RuleBaseline> constraints = new TreeMap<>();

    /**
     * Represent a baseline for a specific {@link com.buschmais.jqassistant.core.rule.api.model.Concept} or {@link com.buschmais.jqassistant.core.rule.api.model.Constraint}.
     */
    @Getter
    @ToString
    public static class RuleBaseline {

        /**
         * Holds the row key as key and the columns (as human-readable labels) as values.
         */
        private final SortedMap<String, SortedMap<String, String>> rows = new TreeMap<>();

    }

}
