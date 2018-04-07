package com.buschmais.jqassistant.core.analysis.api.rule;

import com.buschmais.jqassistant.core.shared.annotation.ToBeRemovedInVersion;

/**
 * Defines a constraint to be validated.
 */
public class Constraint extends AbstractExecutableRule {

    protected Constraint() {
    }

    public static Builder builder() {
        return new Builder(new Constraint());
    }

    public static class Builder extends AbstractExecutableRule.Builder<Constraint.Builder, Constraint> {

        protected Builder(Constraint rule) {
            super(rule);
        }

        @Deprecated
        @ToBeRemovedInVersion(major = 1, minor = 5)
        public static Builder newConstraint() {
            return new Builder(new Constraint());
        }
    }
}
