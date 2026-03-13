package com.buschmais.jqassistant.core.rule.api.filter;

import java.util.*;

import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class RuleFilterTest {

    @Test
    void filter() {
        List<String> rules = asList("wildcard", "foo", "bar");

        SortedSet<String> result = RuleFilter.match(rules, "foo, w?ldc*d");

        assertThat(result).containsExactly("foo", "wildcard");
    }

    @Test
    void negation() {
        List<String> rules = asList("foo", "bar");

        SortedSet<String> result = RuleFilter.match(rules, "*, !b*r");

        assertThat(result).containsExactly("foo");
    }
}
