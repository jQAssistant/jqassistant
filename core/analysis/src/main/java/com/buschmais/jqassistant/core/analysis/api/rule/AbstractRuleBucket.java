package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public abstract class AbstractRuleBucket<T extends AbstractRule, NRE extends NoRuleException,
                                         DRE extends DuplicateRuleException> {
    TreeMap<String, T> rules = new TreeMap<>();

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

    protected Collection<T> getAll() {
        return Collections.unmodifiableCollection(rules.values());
    }

    protected abstract DRE newDuplicateRuleException(String message);

    protected abstract NRE newNoRuleException(String message);

    protected Set<String> getRuleIds() {
        return Collections.unmodifiableSet(rules.keySet());
    }

    protected T get(String id) throws NRE {
        T rule = rules.get(id);

        if (null == rule) {
            throw newNoRuleException(id);
        }

        return rule;
    }

    protected <B extends AbstractRuleBucket<T, NRE, DRE>> void addAll(B bucket) throws DRE {
        String id = null;
        try {
            for (String conceptId : bucket.getRuleIds()) {
                id = conceptId;
                T rule = null;
                rule = bucket.get(id);
                add(rule);
            }
        } catch (NoRuleException e) {
            throw new IllegalStateException("No rule of type " + getRuleTypeName() + " width  " + id +
                                            " not found in overhanded bucket. The bucket is in an inconsistent state.");
        }
    }
}
