package com.buschmais.jqassistant.core.analysis.api.rule;

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

}
