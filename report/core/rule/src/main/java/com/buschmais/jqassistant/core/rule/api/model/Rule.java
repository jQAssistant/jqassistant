package com.buschmais.jqassistant.core.rule.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.rule.api.source.RuleSource;

/**
 * Interface for all rules.
 */
public interface Rule {

    /**
     * Return the id of the rule.
     *
     * @return The id of the rule.
     */
    String getId();

    /**
     * Return the description of the rule.
     *
     * @return The description of the rule.
     */
    String getDescription();

    /**
     * Return the source of the rule.
     *
     * @return The source of the rule.
     */
    RuleSource getSource();

    /**
     * Return the (optional) deprecation message.
     *
     * @return The deprecation message.
     */
    String getDeprecation();

    /**
     * Return the ids of rules which are overridden by this rule.
     *
     * @return The deprecation message.
     */
    List<String> getOverriddenIds();

}
