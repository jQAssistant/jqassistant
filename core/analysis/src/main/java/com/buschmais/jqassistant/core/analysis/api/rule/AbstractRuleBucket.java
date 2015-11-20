package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeMap;

/**
 * Container to store {@link Rule Rules}.
 *
 * @see Rule
 * @param <T> Type of the rule stored in the bucket.
 * @param <NRE> Type of the exception to be thrown if the requested rule of
 *              type {@code T} could not be found.
 * @param <DRE> Type of the exception to be thrown if a rule is added
 *              twice to the bucket.
 */
public abstract class AbstractRuleBucket<T extends AbstractRule, NRE extends NoRuleException,
                                         DRE extends DuplicateRuleException> {
    TreeMap<String, T> rules = new TreeMap<>();

    /**
     * Returns the number of rules of type {@code T} contained in the bucket.
     *
     * @return Number of rules in the bucket.
     */
    public int size() {
        return rules.size();
    }

    protected void add(T rule) throws DRE {
        if (rules.containsKey(rule.getId())) {
            throw newDuplicateRuleException("The " + getRuleTypeName() + " " + rule.getId() +
                                             " is already contained in this bucket");
        } else {
            rules.put(rule.getId(), rule);
        }
    }

    protected abstract String getRuleTypeName();

    /**
     * Returns a unmodifiable collection of all rules contained
     * in the bucket.
     *
     * @return Collection with all rules in the container.
     */
    public Collection<T> getAll() {
        return Collections.unmodifiableCollection(rules.values());
    }

    protected abstract DRE newDuplicateRuleException(String message);

    protected abstract NRE newNoRuleException(String message);

    /**
     * Returns a set containing the ids of all rules contained in the bucket.
     *
     * @return a set of ids of all rules in the bucket.
     */
    public Set<String> getIds() {
        return Collections.unmodifiableSet(rules.keySet());
    }

    /**
     * Returns a rule of type {@code T} by its id.
     *
     * @return The rule with the requested id.
     *
     * @throws <NRE> if the requested rule cannot be found.
     */
    public T getById(String id) throws NRE {
        T rule = rules.get(id);

        if (null == rule) {
            throw newNoRuleException(id);
        }

        return rule;
    }

    public <B extends AbstractRuleBucket<T, NRE, DRE>> void add(B bucket) throws DRE {
        String id = null;
        try {
            for (String conceptId : bucket.getIds()) {
                id = conceptId;
                T rule = null;
                rule = bucket.getById(id);
                add(rule);
            }
        } catch (NoRuleException e) {
            throw new IllegalStateException("No rule of type " + getRuleTypeName() + " width  " + id +
                                            " not found in overhanded bucket. The bucket is in an inconsistent state.");
        }
    }
}
