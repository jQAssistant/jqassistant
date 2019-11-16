package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.*;

import com.buschmais.jqassistant.core.rule.api.filter.RuleFilter;

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
    private TreeMap<String, T> rules = new TreeMap<>();

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

    /**
     * Matches the rules in this bucket against the given pattern that might contain
     * wildcards, i.e. '*' and '?'.
     *
     * @param pattern
     *            The pattern.
     * @return The list of matching rules.
     * @throws NoRuleException
     *             If the pattern did not contained any wildcard and the referenced
     *             rule is not contained in the bucket.
     */
    public List<T> match(String pattern) throws NoRuleException {
        List<T> matches = new ArrayList<>();
        if (pattern.contains("?") || pattern.contains("*")) {
            for (Map.Entry<String, T> entry : rules.entrySet()) {
                if (RuleFilter.getInstance().matches(entry.getKey(), pattern)) {
                    matches.add(entry.getValue());
                }
            }
        } else {
            matches.add(getById(pattern));
        }
        return matches;
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
