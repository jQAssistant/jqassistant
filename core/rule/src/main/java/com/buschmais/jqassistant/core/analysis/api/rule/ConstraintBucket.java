package com.buschmais.jqassistant.core.analysis.api.rule;

/**
 * Container for a set of unique constraints.
 *
 * A collection of unique constraint rules identified by their id. That means that this bucket
 * cannot contain a constraint with the same id twice.
 */
public class ConstraintBucket extends AbstractRuleBucket<Constraint, NoConstraintException, DuplicateConstraintException> {
    @Override
    protected String getRuleTypeName() {
        return "constraint";
    }

    @Override
    protected NoConstraintException newNoRuleException(String message) {
        return new NoConstraintException(message);
    }

    @Override
    protected DuplicateConstraintException newDuplicateRuleException(String message) {
        return new DuplicateConstraintException(message);
    }
}
