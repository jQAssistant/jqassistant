package com.buschmais.jqassistant.core.analysis.api.rule;

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
    public String getDescription();

}
