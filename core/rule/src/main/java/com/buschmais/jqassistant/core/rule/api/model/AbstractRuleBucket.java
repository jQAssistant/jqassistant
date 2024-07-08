package com.buschmais.jqassistant.core.rule.api.model;

import java.util.*;

import com.buschmais.jqassistant.core.rule.api.filter.RuleFilter;

/**
 * Container to store {@link Rule Rules}.
 *
 * @param <T>
 *            Type of the rule stored in the bucket.
 * @see Rule
 */
public abstract class AbstractRuleBucket<T extends AbstractRule> {

    private TreeMap<String, T> rules = new TreeMap<>();

    /**
     * Returns the number of rules of type {@code T} contained in the bucket.
     *
     * @return Number of rules in the bucket.
     */
    public int size() {
        return rules.size();
    }

    protected void add(T rule) throws RuleException {
        T existingRule = rules.put(rule.getId(), rule);
        if (existingRule != null) {
            throw new RuleException("Cannot add rule with id '" + rule.getId() + "' from '" + rule.getSource().getId() + "' as it has already been defined in '"
                    + existingRule.getSource().getId() + "'.");
        }
    }

    protected abstract String getRuleTypeName();

    /**
     * Returns a unmodifiable collection of all rules contained in the bucket.
     *
     * @return Collection with all rules in the container.
     */
    public Collection<T> getAll() {
        return Collections.unmodifiableCollection(rules.values());
    }

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
     * @throws RuleException
     *             if the requested rule cannot be found.
     */
    public T getById(String id) throws RuleException {
        T rule = rules.get(id);

        if (null == rule) {
            throw new RuleException("Cannot find " + getRuleTypeName() + " " + id);
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
     * @throws RuleException
     *             If the pattern did not contained any wildcard and the referenced
     *             rule is not contained in the bucket.
     */
    public List<T> match(String pattern) throws RuleException {
        List<T> matches = new ArrayList<>();
        if (pattern.contains("?") || pattern.contains("*")) {
            for (Map.Entry<String, T> entry : rules.entrySet()) {
                if (RuleFilter.matches(entry.getKey(), pattern)) {
                    matches.add(entry.getValue());
                }
            }
        } else {
            matches.add(getById(pattern));
        }
        return matches;
    }

    public <B extends AbstractRuleBucket<T>> void add(B bucket) throws RuleException {
        for (String ruleId : bucket.getIds()) {
            T rule = bucket.getById(ruleId);
            add(rule);
        }
    }
}
