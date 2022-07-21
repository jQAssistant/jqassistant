package com.buschmais.jqassistant.core.rule.api.filter;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;

import org.apache.commons.io.FilenameUtils;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

/**
 * A filter for rules.
 */
public final class RuleFilter {

    private RuleFilter() {
    }

    /**
     * Match rule ids by the given filter.
     * <p>
     * Accepts a comma separated list of filter patterns, each one may contain
     * wildcards "*" or "?". Any pattern starting with "!" will be interpreted as
     * exlcusion.
     *
     * @param rules
     *     The rules ids to match.
     * @param filter
     *     The filter.
     * @return The matching rule ids.
     */
    public static SortedSet<String> match(Iterable<String> rules, String filter) {
        SortedSet<String> matches = new TreeSet<>();
        if (filter != null) {
            List<String> rulePatterns = asList(filter.split("\\s*,\\s*"));

            List<String> includePatterns = rulePatterns.stream()
                .filter(pattern -> !pattern.startsWith("!"))
                .collect(toList());
            List<String> excludePatterns = rulePatterns.stream()
                .filter(pattern -> pattern.startsWith("!"))
                .map(pattern -> pattern.substring(1))
                .collect(toList());

            apply(rules, includePatterns, rule -> matches.add(rule));
            apply(rules, excludePatterns, rule -> matches.remove(rule));
        }
        return matches;
    }

    private static void apply(Iterable<String> rules, List<String> patterns, Consumer<String> consumer) {
        for (String rule : rules) {
            for (String pattern : patterns) {
                if (matches(rule, pattern)) {
                    consumer.accept(rule);
                }
            }
        }
    }

    public static boolean matches(String rule, String pattern) {
        return FilenameUtils.wildcardMatch(rule, pattern);
    }
}
