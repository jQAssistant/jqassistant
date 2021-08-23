package com.buschmais.jqassistant.core.rule.api.filter;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RuleFilterTest {

    private RuleFilter ruleFilter = RuleFilter.getInstance();

    @Test
    void filter() {
        Set<String> rules = new HashSet<>();
        rules.add("foo");
        rules.add("bar");
        rules.add("wildcard");

        Set<String> result = ruleFilter.match(rules, "foo, w?ldc*d");

        assertThat(result).containsOnly("foo", "wildcard");
    }

    @Test
    void negation() {
        Set<String> rules = new HashSet<>();
        rules.add("foo");
        rules.add("bar");

        Set<String> result = ruleFilter.match(rules, "*, !b*r");

        assertThat(result).containsExactly("foo");
    }
}
