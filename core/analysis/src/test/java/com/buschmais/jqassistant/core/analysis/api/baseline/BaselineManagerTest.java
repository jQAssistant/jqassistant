package com.buschmais.jqassistant.core.analysis.api.baseline;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

import com.buschmais.jqassistant.core.report.api.model.Column;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class BaselineManagerTest {

    private static Stream<Arguments> rules() {
        return Stream.of(Arguments.of(Concept.builder()
            .id("concept")
            .build()), Arguments.of(Constraint.builder()
            .id("constraint")
            .build()));
    }

    @Mock
    private com.buschmais.jqassistant.core.analysis.api.configuration.Baseline configuration;

    @ParameterizedTest
    @MethodSource("rules")
    void noBaselineWithNewConstraintViolation(ExecutableRule<?> executableRule) {
        Row row = Row.builder()
            .key("1")
            .columns(Map.of("c1", Column.builder()
                .label("1")
                .build()))
            .build();
        BaselineManager baselineManager = new BaselineManager(configuration,empty());

        assertThat(baselineManager.isNew(executableRule, row)).isTrue();

        verifyNewBaseline(executableRule, baselineManager.getNewBaseline()
            .getConstraints(), "1");
    }

    @ParameterizedTest
    @MethodSource("rules")
    void existingBaselineWithExistingConstraintViolation(ExecutableRule<?> executableRule) {
        Baseline oldBaseline = createOldBaseline(executableRule, "1");
        Row row = Row.builder()
            .key("1")
            .columns(Map.of("c1", Column.builder()
                .label("1")
                .build()))
            .build();
        BaselineManager baselineManager = new BaselineManager(configuration, of(oldBaseline));

        assertThat(baselineManager.isNew(executableRule, row)).isFalse();

        verifyNewBaseline(executableRule, baselineManager.getNewBaseline()
            .getConstraints(), "1");
    }

    @ParameterizedTest
    @MethodSource("rules")
    void existingBaselineWithNewConstraintViolation(ExecutableRule<?> executableRule) {
        Baseline oldBaseline = createOldBaseline(executableRule, "1");
        Row oldRow = Row.builder()
            .key("1")
            .columns(Map.of("c1", Column.builder()
                .label("1")
                .build()))
            .build();
        Row newRow = Row.builder()
            .key("2")
            .columns(Map.of("c1", Column.builder()
                .label("2")
                .build()))
            .build();
        BaselineManager baselineManager = new BaselineManager(configuration, of(oldBaseline));

        assertThat(baselineManager.isNew(executableRule, oldRow)).isFalse();
        assertThat(baselineManager.isNew(executableRule, newRow)).isTrue();

        verifyNewBaseline(executableRule, baselineManager.getNewBaseline()
            .getConstraints(), "1");
    }

    @ParameterizedTest
    @MethodSource("rules")
    void existingBaselineWithRemovedConstraintViolation(ExecutableRule<?> executableRule) {
        Baseline oldBaseline = createOldBaseline(executableRule, "1", "2");
        Row row = Row.builder()
            .key("1")
            .columns(Map.of("c1", Column.builder()
                .label("1")
                .build()))
            .build();
        BaselineManager baselineManager = new BaselineManager(configuration, of(oldBaseline));

        assertThat(baselineManager.isNew(executableRule, row)).isFalse();

        verifyNewBaseline(executableRule, baselineManager.getNewBaseline()
            .getConstraints(), "1");
    }

    private static Baseline createOldBaseline(ExecutableRule<?> rule, String... rowKeys) {
        Baseline.RuleBaseline oldRuleBaseline = new Baseline.RuleBaseline();
        for (String rowKey : rowKeys) {
            TreeMap<String, String> row = new TreeMap<>();
            row.put("c1", rowKey);
            oldRuleBaseline.getRows()
                .put(rowKey, row);
        }
        Baseline oldBaseline = new Baseline();
        SortedMap<String, Baseline.RuleBaseline> ruleBaselines = rule instanceof Concept ? oldBaseline.getConcepts() : oldBaseline.getConstraints();
        ruleBaselines.put(rule.getId(), oldRuleBaseline);
        return oldBaseline;
    }

    private static void verifyNewBaseline(ExecutableRule<?> executableRule, SortedMap<String, Baseline.RuleBaseline> rulebaseLines, String... expectedRowKeys) {
        assertThat(rulebaseLines).hasSize(1)
            .containsKey(executableRule.getId());
        Baseline.RuleBaseline ruleBaseline = rulebaseLines.get(executableRule.getId());
        assertThat(ruleBaseline).isNotNull();
        SortedMap<String, SortedMap<String, String>> rows = ruleBaseline.getRows();
        assertThat(rows).hasSize(expectedRowKeys.length);
        for (String expectedRowKey : expectedRowKeys) {
            assertThat(rows.get(expectedRowKey)).containsExactly(Map.entry("c1", expectedRowKey));
        }
    }
}
