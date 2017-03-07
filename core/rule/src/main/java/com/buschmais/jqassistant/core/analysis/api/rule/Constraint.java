package com.buschmais.jqassistant.core.analysis.api.rule;

/**
 * Defines a constraint to be validated.
 */
public class Constraint extends AbstractExecutableRule {

    protected Constraint() {
    }

    public static class Builder extends AbstractExecutableRule.Builder<Constraint.Builder, Constraint> {

        protected Builder(Constraint rule) {
            super(rule);
        }

        public static Builder newConstraint() {
            return new Builder(new Constraint());
        }
    }
}
