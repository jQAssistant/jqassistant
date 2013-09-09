package com.buschmais.jqassistant.core.analysis.api;

import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;

import java.util.List;

/**
 * Defines the interface for selecting rules sets from given names.
 */
public interface RuleSelector {

    String DEFAULT_GROUP = "default";

    /**
     * Determines the effective rules set from the given rules set, concept names, constraint names and group names.
     *
     * @param ruleSet
     * @param conceptNames
     * @param constraintNaimes
     * @param groupNames
     * @return The effective rules set.
     * @throws RuleSetResolverException If resolving fails.
     */
    public RuleSet getEffectiveRuleSet(RuleSet ruleSet, List<String> conceptNames, List<String> constraintNaimes, List<String> groupNames) throws RuleSetResolverException;

}
