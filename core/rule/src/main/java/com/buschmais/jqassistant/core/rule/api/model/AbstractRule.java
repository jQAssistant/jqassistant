package com.buschmais.jqassistant.core.rule.api.model;

import com.buschmais.jqassistant.core.rule.api.source.RuleSource;

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

    /**
     * The rule source.
     */
    private RuleSource ruleSource;

    /**
     * The optional deprecation message.
     */
    private String deprecation;

    @Override
    public String getId() {
        return id;
    }

    void setId(String id) {
        this.id = id;
    }

    @Override
    public String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }


    @Override
    public RuleSource getSource() {
        return ruleSource;
    }

    void setSource(RuleSource ruleSource) {
        this.ruleSource = ruleSource;
    }


    @Override
    public String getDeprecation() {
        return deprecation;
    }

    void setDeprecation(String deprecation) {
        this.deprecation = deprecation;
    }


    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof AbstractRule)) {
            return false;
        }

        AbstractRule that = (AbstractRule) o;

        return this.getClass().equals(that.getClass()) && id.equals(that.id);
    }

    @Override
    public final int hashCode() {
        return id.hashCode();
    }

    @Override
    public final String toString() {
        return this.getClass().getSimpleName() + "{" + "id='" + id + '\'' + ", description='" + description + '\'' + ", ruleSource=" + ruleSource + '}';
    }

    protected abstract static class Builder<B extends Builder<B, R>, R extends AbstractRule> {

        protected R rule;

        protected abstract B getThis();

        protected Builder(R rule) {
            this.rule = rule;
        }

        public R build() {
            return rule;
        }

        public B id(String id) {
            rule.setId(id);
            return getThis();
        }

        public B description(String description) {
            rule.setDescription(description);
            return getThis();
        }

        public B ruleSource(RuleSource ruleSource) {
            rule.setSource(ruleSource);
            return getThis();
        }

        public B deprecation(String deprecation) {
            rule.setDeprecation(deprecation);
            return getThis();
        }
    }
}
