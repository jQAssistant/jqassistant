package com.buschmais.jqassistant.core.analysis.api.rule;

import com.buschmais.jqassistant.core.rule.api.source.RuleSource;
import com.buschmais.jqassistant.core.shared.annotation.ToBeRemovedInVersion;

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

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public RuleSource getSource() {
        return ruleSource;
    }

    @Override
    public String getDeprecation() {
        return deprecation;
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

        return id.equals(that.id);
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

        protected AbstractRule rule;

        protected Builder(R rule) {
            this.rule = rule;
        }

        @Deprecated
        @ToBeRemovedInVersion(major = 1, minor = 5)
        public R get() {
            return build();
        }

        public R build () {
            return (R) rule;
        }

        protected B builder() {
            return (B) this;
        }

        public B id(String id) {
            rule.id = id;
            return builder();
        }

        public B description(String description) {
            rule.description = description;
            return builder();
        }

        public B ruleSource(RuleSource ruleSource) {
            rule.ruleSource = ruleSource;
            return builder();
        }

        public B deprecation(String deprecation) {
            rule.deprecation = deprecation;
            return builder();
        }
    }
}
