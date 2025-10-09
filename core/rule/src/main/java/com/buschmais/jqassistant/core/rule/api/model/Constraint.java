package com.buschmais.jqassistant.core.rule.api.model;

import org.jqassistant.schema.rule.v2.ReferenceType;

import static com.buschmais.jqassistant.core.rule.api.model.Severity.MAJOR;

/**
 * Defines a constraint to be validated.
 */
public class Constraint extends AbstractExecutableRule {

    public static Severity DEFAULT_SEVERITY = MAJOR;

    public static class ConstraintBuilder extends AbstractExecutableRule.Builder<ConstraintBuilder, Constraint> {
        public ConstraintBuilder(Constraint rule) {
            super(rule);
        }

        @Override
        protected ConstraintBuilder getThis() {
            return this;
        }

        public Constraint.ConstraintBuilder overrideConstraint(ReferenceType overrideConstraint) {
            Constraint r = build();
            if (overrideConstraint != null) {
                r.setOverriddenId(overrideConstraint.getRefId());
            }
            return this;
        }
    }

    public static ConstraintBuilder builder() {
        return new ConstraintBuilder(new Constraint());
    }
}
