package com.buschmais.jqassistant.core.analysis.api.baseline;

import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import com.buschmais.jqassistant.core.report.api.model.Column;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;

import lombok.RequiredArgsConstructor;

/**
 * Verifies result rows gathered for rule during an analyze task against a baseline
 * <p>
 * {@link Row}s of a constraint are considered as "new" if
 * <ul>
 *     <li>No baseline exists.</li>
 *     <li>A baseline exists but does not yet contain the {@link Row}.</li>
 * </ul>
 * <p>
 * Only {@link Row}s that have been validated using {@link #isNew(ExecutableRule, Row)} ({@link ExecutableRule}, Row)} are copied to the new baseline.
 * <p>
 */
@RequiredArgsConstructor
public class BaselineManager {

    private final com.buschmais.jqassistant.core.analysis.api.configuration.Baseline configuration;

    private final Optional<Baseline> optionalOldBaseline;

    private final Baseline newBaseline = new Baseline();

    public boolean isNew(ExecutableRule<?> executableRule, Row row) {
        String ruleId = executableRule.getId();
        String rowKey = row.getKey();
        Map<String, Column<?>> columns = row.getColumns();
        return optionalOldBaseline.map(oldBaseline -> {
                SortedMap<String, Baseline.RuleBaseline> ruleBaseline = getRows(oldBaseline, executableRule);
                Baseline.RuleBaseline oldRuleBaseline = ruleBaseline.get(ruleId);
                if (oldRuleBaseline != null && oldRuleBaseline.getRows()
                    .containsKey(rowKey)) {
                    add(ruleId, rowKey, columns);
                    return false;
                }
                return true;
            })
            .orElseGet(() -> {
                add(ruleId, rowKey, columns);
                return true;
            });
    }

    private static SortedMap<String, Baseline.RuleBaseline> getRows(Baseline baseline, ExecutableRule<?> executableRule) {
        if (executableRule instanceof Concept) {
            return baseline.getConcepts();
        } else if (executableRule instanceof Constraint) {
            return baseline.getConstraints();
        }
        throw new IllegalArgumentException("Unsupported executable rule: " + executableRule);
    }

    private void add(String constraintId, String rowKey, Map<String, Column<?>> columns) {
        Baseline.RuleBaseline newRuleBaseline = newBaseline.getConstraints()
            .computeIfAbsent(constraintId, key -> new Baseline.RuleBaseline());
        TreeMap<String, String> row = new TreeMap<>();
        columns.entrySet()
            .stream()
            .forEach(entry -> row.put(entry.getKey(), entry.getValue()
                .getLabel()));
        newRuleBaseline.getRows()
            .put(rowKey, row);
    }

    public Baseline getNewBaseline() {
        return newBaseline;
    }
}
