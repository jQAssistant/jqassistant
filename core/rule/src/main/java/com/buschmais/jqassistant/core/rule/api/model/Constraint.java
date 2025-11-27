package com.buschmais.jqassistant.core.rule.api.model;

import java.util.List;
import java.util.stream.Collectors;

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

        public Constraint.ConstraintBuilder overrideConstraints(List<ReferenceType> overrideConstraints) {
            Constraint r = build();
            List<String> ids = overrideConstraints.stream()
                .map(ReferenceType::getRefId)
                .collect(Collectors.toList());
            r.setOverriddenIds(ids);
            return this;
        }
    }

    public static ConstraintBuilder builder() {
        return new ConstraintBuilder(new Constraint());
    }
}
