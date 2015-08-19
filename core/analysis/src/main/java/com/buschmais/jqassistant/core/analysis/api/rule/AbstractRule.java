package com.buschmais.jqassistant.core.analysis.api.rule;

import com.buschmais.jqassistant.core.analysis.api.rule.source.RuleSource;

/**
 * Abstract base class for rules.
 */
public abstract class AbstractRule implements Rule {

    /**
     * The id of the rule.
     */
    private String id;

    /**
     * The optional description.
     */
    private String description;

    private RuleSource ruleSource;

    /**
     * Constructor.
     * 
     * @param id
     *            The id of the rule.
     * @param description
     *            The descripton of the rule.
     * @param ruleSource
     *            The rule source.
     */
    public AbstractRule(String id, String description, RuleSource ruleSource) {
        this.id = id;
        this.description = description;
        this.ruleSource = ruleSource;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public RuleSource getSource() {
        return ruleSource;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AbstractRule))
            return false;

        AbstractRule that = (AbstractRule) o;

        return id.equals(that.id);

    }

    @Override
    public final int hashCode() {
        return id.hashCode();
    }

    @Override
    public final String toString() {
        return "AbstractRule{" + "id='" + id + '\'' + ", description='" + description + '\'' + ", ruleSource=" + ruleSource + '}';
    }
}
