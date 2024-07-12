package com.buschmais.jqassistant.core.analysis.api.baseline;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Stream;

import com.buschmais.jqassistant.core.report.api.model.Column;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @Mock
    private BaselineRepository baselineRepository;

    @Captor
    private ArgumentCaptor<Baseline> baselineArgumentCaptor;

    private BaselineManager baselineManager;

    @BeforeEach
    void setUp() {
        this.baselineManager = new BaselineManager(configuration, baselineRepository);
    }

    @ParameterizedTest
    @MethodSource("rules")
    void baselineDisabled(ExecutableRule<?> executableRule) {
        doReturn(false).when(configuration)
            .enabled();
        Row row = Row.builder()
            .key("1")
            .columns(Map.of("c1", Column.builder()
                .label("1")
                .build()))
            .build();

        baselineManager.start();
        assertThat(baselineManager.isExisting(executableRule, row)).isFalse();
        baselineManager.stop();

        verify(baselineRepository, never()).read();
        verify(baselineRepository, never()).write(any(Baseline.class));
    }

    @ParameterizedTest
    @MethodSource("rules")
    void noBaselineWithNewConstraintViolation(ExecutableRule<?> executableRule) {
        doReturn(true).when(configuration)
            .enabled();
        doReturn(empty()).when(baselineRepository)
            .read();
        Row row = Row.builder()
            .key("1")
            .columns(Map.of("c1", Column.builder()
                .label("1")
                .build()))
            .build();

        baselineManager.start();
        assertThat(baselineManager.isExisting(executableRule, row)).isFalse();
        baselineManager.stop();

        verifyNewBaseline(executableRule, baseline -> baseline.getConstraints(), "1");
    }

    @ParameterizedTest
    @MethodSource("rules")
    void existingBaselineWithExistingConstraintViolation(ExecutableRule<?> executableRule) {
        doReturn(true).when(configuration)
            .enabled();
        Baseline oldBaseline = createOldBaseline(executableRule, "1");
        doReturn(of(oldBaseline)).when(baselineRepository)
            .read();
        Row row = Row.builder()
            .key("1")
            .columns(Map.of("c1", Column.builder()
                .label("1")
                .build()))
            .build();

        baselineManager.start();
        assertThat(baselineManager.isExisting(executableRule, row)).isTrue();
        baselineManager.stop();

        verifyNewBaseline(executableRule, baseline -> baseline.getConstraints(), "1");
    }

    @ParameterizedTest
    @MethodSource("rules")
    void existingBaselineWithNewConstraintViolation(ExecutableRule<?> executableRule) {
        doReturn(true).when(configuration)
            .enabled();
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
        doReturn(of(oldBaseline)).when(baselineRepository)
            .read();

        baselineManager.start();
        assertThat(baselineManager.isExisting(executableRule, oldRow)).isTrue();
        assertThat(baselineManager.isExisting(executableRule, newRow)).isFalse();
        baselineManager.stop();

        verifyNewBaseline(executableRule, baseline -> baseline.getConstraints(), "1");
    }

    @ParameterizedTest
    @MethodSource("rules")
    void existingBaselineWithRemovedConstraintViolation(ExecutableRule<?> executableRule) {
        doReturn(true).when(configuration)
            .enabled();
        Baseline oldBaseline = createOldBaseline(executableRule, "1", "2");
        Row row = Row.builder()
            .key("1")
            .columns(Map.of("c1", Column.builder()
                .label("1")
                .build()))
            .build();
        doReturn(of(oldBaseline)).when(baselineRepository)
            .read();

        baselineManager.start();
        assertThat(baselineManager.isExisting(executableRule, row)).isTrue();
        baselineManager.stop();

        verifyNewBaseline(executableRule, baseline -> baseline.getConstraints(), "1");
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

    private void verifyNewBaseline(ExecutableRule<?> executableRule, Function<Baseline, SortedMap<String, Baseline.RuleBaseline>> rulebaseLinesFunction,
        String... expectedRowKeys) {
        verify(baselineRepository).write(baselineArgumentCaptor.capture());
        Baseline newBaseline = baselineArgumentCaptor.getValue();
        SortedMap<String, Baseline.RuleBaseline> ruleBaselines = rulebaseLinesFunction.apply(newBaseline);
        assertThat(ruleBaselines).hasSize(1)
            .containsKey(executableRule.getId());
        Baseline.RuleBaseline ruleBaseline = ruleBaselines.get(executableRule.getId());
        assertThat(ruleBaseline).isNotNull();
        SortedMap<String, SortedMap<String, String>> rows = ruleBaseline.getRows();
        assertThat(rows).hasSize(expectedRowKeys.length);
        for (String expectedRowKey : expectedRowKeys) {
            assertThat(rows.get(expectedRowKey)).containsExactly(Map.entry("c1", expectedRowKey));
        }
    }
}
