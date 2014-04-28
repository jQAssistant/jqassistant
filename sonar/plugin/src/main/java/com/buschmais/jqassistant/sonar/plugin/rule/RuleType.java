package com.buschmais.jqassistant.sonar.plugin.rule;

import org.sonar.api.rules.RulePriority;

/**
 * The rule types supported by jQAssistant.
 */
public enum RuleType {

    Concept(RulePriority.MAJOR), Constraint(RulePriority.CRITICAL);

    private RulePriority priority;

    /**
     * Constructor.
     * 
     * @param priority
     *            The default priority.
     */
    private RuleType(RulePriority priority) {
        this.priority = priority;
    }

    /**
     * Return the default priority.
     * 
     * @return The default priority.
     */
    public RulePriority getPriority() {
        return priority;
    }
}
