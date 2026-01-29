package com.buschmais.jqassistant.core.analysis.api.baseline;

import java.util.*;
import java.util.function.Function;

import com.buschmais.jqassistant.core.report.api.model.Column;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.rule.api.filter.RuleFilter;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.util.Collections.emptyList;

/**
 * Verifies result rows gathered for rule during an analyze task against a baseline
 * <p>
 * {@link Row}s of a constraint are considered as "new" if
 * <ul>
 *     <li>No baseline exists.</li>
 *     <li>A baseline exists but does not yet contain the {@link Row}.</li>
 * </ul>
 * <p>
 * Only {@link Row}s that have been validated using {@link #isExisting(ExecutableRule, String, Map<String, Column<?>>)} ({@link ExecutableRule}, String, Map<String, Column<?>>)} are copied to the new baseline.
 * <p>
 */
@RequiredArgsConstructor
@Slf4j
public class BaselineManager {

    private final com.buschmais.jqassistant.core.analysis.api.configuration.Baseline configuration;

    private final BaselineRepository baselineRepository;

    private Optional<Baseline> optionalOldBaseline;

    private Baseline newBaseline;

    public void start() {
        if (configuration.enabled()) {
            optionalOldBaseline = baselineRepository.read();
            newBaseline = new Baseline();
        }
    }

    public void stop() {
        if (configuration.enabled() && !(optionalOldBaseline.isPresent() && newBaseline.equals(optionalOldBaseline.get()))) {
            log.info("Baseline has been updated.");
            baselineRepository.write(newBaseline);
        }
    }

    public boolean isExisting(ExecutableRule<?> executableRule, String rowKey, Map<String, Column<?>> columns) {
        if (!configuration.enabled()) {
            return false;
        }
        if (newBaseline == null) {
            throw new IllegalStateException("Baseline manager has not been started yet");
        }
        if (executableRule instanceof Concept) {
            return isExistingResult(executableRule, rowKey, columns, configuration.includeConcepts()
                .orElse(emptyList()), Baseline::getConcepts);
        } else if (executableRule instanceof Constraint) {
            return isExistingResult(executableRule, rowKey, columns, configuration.includeConstraints(), Baseline::getConstraints);
        }
        throw new IllegalArgumentException("Unsupported executable rule: " + executableRule);
    }

    private Boolean isExistingResult(ExecutableRule<?> executableRule, String rowKey, Map<String, Column<?>> columns, List<String> ruleFilters,
        Function<Baseline, SortedMap<String, Baseline.RuleBaseline>> rows) {
        String ruleId = executableRule.getId();
        if (ruleFilters.stream()
            .noneMatch(filter -> RuleFilter.matches(ruleId, filter))) {
            return false;
        }
        return optionalOldBaseline.map(oldBaseline -> {
                SortedMap<String, Baseline.RuleBaseline> ruleBaseline = rows.apply(oldBaseline);
                Baseline.RuleBaseline oldRuleBaseline = ruleBaseline.get(ruleId);
                if (oldRuleBaseline != null && oldRuleBaseline.getRows()
                    .containsKey(rowKey)) {
                    addToNewBaseline(ruleId, rowKey, columns, rows);
                    return true;
                }
                return false;
            })
            .orElseGet(() -> {
                addToNewBaseline(ruleId, rowKey, columns, rows);
                return false;
            });
    }

    private void addToNewBaseline(String constraintId, String rowKey, Map<String, Column<?>> columns,
        Function<Baseline, SortedMap<String, Baseline.RuleBaseline>> rows) {
        Baseline.RuleBaseline newRuleBaseline = rows.apply(newBaseline)
            .computeIfAbsent(constraintId, key -> new Baseline.RuleBaseline());
        TreeMap<String, String> row = new TreeMap<>();
        columns.entrySet()
            .stream()
            .forEach(entry -> row.put(entry.getKey(), entry.getValue()
                .getLabel()));
        newRuleBaseline.getRows()
            .put(rowKey, row);
    }
}
