package com.buschmais.jqassistant.core.analysis.api;

import com.buschmais.jqassistant.core.model.api.rules.RuleSet;

import java.util.List;

/**
 * Defines the interface for resolving rule sets from given names.
 */
public interface RuleSetResolver {

    String DEFAULT_GROUP = "default";

    /**
     * Determines the effective rule set from the given rule set, concept names, constraint names and group names.
     *
     * @param ruleSet
     * @param conceptNames
     * @param constraintNaimes
     * @param groupNames
     * @return The effective rule set.
     * @throws RuleSetResolverException If resolving fails.
     */
    public RuleSet getEffectiveRuleSet(RuleSet ruleSet, List<String> conceptNames, List<String> constraintNaimes, List<String> groupNames) throws RuleSetResolverException;

}
