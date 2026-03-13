package com.buschmais.jqassistant.core.rule.api.model;

/**
 * Container for a set of unique constraints.
 *
 * A collection of unique constraint rules identified by their id. That means that this bucket
 * cannot contain a constraint with the same id twice.
 */
public class ConstraintBucket extends AbstractRuleBucket<Constraint> {

    @Override
    protected String getRuleTypeName() {
        return "constraint";
    }

}
