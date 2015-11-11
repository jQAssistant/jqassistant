package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Collection;
import java.util.Set;

/**
 * Container for a set of unique constraints.
 *
 * <p>
 *      A collection of unique constraint rules identified by their id. That means that this bucket
 *      cannot contain a constraint with the same id twice.
 * </p>
 */
public class ConstraintBucket extends AbstractRuleBucket<Constraint, NoConstraintException, DuplicateConstraintException> {
    @Override
    protected String getRuleTypeName() {
        return "constraint";
    }

    public void addConstraint(Constraint constraint) throws DuplicateConstraintException {
        add(constraint);
    }

    /**
     * Returns a unmodifiable set with all concept ids.
     *
     * @return a set with all concept ids. Result will never be {@code null}.
     */
    public Set<String> getConstraintIds() {
        return getRuleIds();
    }

    public Constraint getConstraint(String id) throws NoConstraintException {
        return get(id);
    }

    public void addConstraints(ConstraintBucket bucket) throws DuplicateConstraintException {
        addAll(bucket);
    }

    @Override
    protected NoConstraintException newNoRuleException(String message) {
        return new NoConstraintException(message);
    }

    @Override
    protected DuplicateConstraintException newDuplicateRuleException(String message) {
        return new DuplicateConstraintException(message);
    }

    public Collection<Constraint> getConstraints() {
        return getAll();
    }
}
