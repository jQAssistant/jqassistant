package com.buschmais.jqassistant.core.analysis.impl;

import java.util.*;

import com.buschmais.jqassistant.core.report.api.model.Column;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.reader.AggregationVerification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.buschmais.jqassistant.core.report.api.ReportHelper.toColumn;
import static com.buschmais.jqassistant.core.report.api.ReportHelper.toRow;
import static com.buschmais.jqassistant.core.report.api.model.Result.Status.FAILURE;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(MockitoExtension.class)
class AggregationVerificationStrategyTest {

    static final List<String> COLUMN_NAMES = asList("c0", "c1");

    static final Map<String, Column<?>> COLUMNS = Map.of("c0", Column.builder()
        .value(1)
        .label("1")
        .sourceLocation(empty())
        .build(), "c1", Column.builder()
        .value("hello")
        .label("hello")
        .sourceLocation(empty())
        .build());

    @Mock
    private Concept concept;

    @Mock
    private Constraint constraint;

    private List<Row> result;

    private AggregationVerificationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new AggregationVerificationStrategy();
    }

    @Test
    void defaultConfiguration() throws RuleException {
        AggregationVerification aggregationVerification = AggregationVerification.builder()
            .build();
        // Columns
        assertThat(strategy.verifyColumns(concept, aggregationVerification, COLUMN_NAMES, COLUMNS)
            .isSuccess()).isTrue();
        assertThat(strategy.verifyColumns(constraint, aggregationVerification, COLUMN_NAMES, COLUMNS)
            .isSuccess()).isFalse();

        // Rows
        result = asList(createRow(concept, 0), createRow(concept, 0));
        assertThat(strategy.verifyRows(concept, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();
        assertThat(strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();

        result = asList(createRow(concept, 0), createRow(concept, 1));
        assertThat(strategy.verifyRows(concept, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();
        assertThat(strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();

        result = asList(createRow(concept, 1), createRow(concept, 1));
        assertThat(strategy.verifyRows(concept, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();
        assertThat(strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();
    }

    @Test
    void min() throws RuleException {
        AggregationVerification aggregationVerification = AggregationVerification.builder()
            .min(1)
            .build();

        // Columns
        assertThat(strategy.verifyColumns(concept, aggregationVerification, COLUMN_NAMES, COLUMNS)
            .isSuccess()).isTrue();
        assertThat(strategy.verifyColumns(constraint, aggregationVerification, COLUMN_NAMES, COLUMNS)
            .isSuccess()).isTrue();

        // Rows
        result = asList(createRow(concept, 0), createRow(concept, 0));
        assertThat(strategy.verifyRows(concept, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();
        assertThat(strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();

        result = asList(createRow(concept, 0), createRow(concept, 1));
        assertThat(strategy.verifyRows(concept, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();
        assertThat(strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();

        result = asList(createRow(concept, 1), createRow(concept, 1));
        assertThat(strategy.verifyRows(concept, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();
        assertThat(strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();
    }

    @Test
    void max() throws RuleException {
        AggregationVerification aggregationVerification = AggregationVerification.builder()
            .max(0)
            .build();

        // Columns
        assertThat(strategy.verifyColumns(concept, aggregationVerification, COLUMN_NAMES, COLUMNS)
            .isSuccess()).isFalse();
        assertThat(strategy.verifyColumns(constraint, aggregationVerification, COLUMN_NAMES, COLUMNS)
            .isSuccess()).isFalse();

        // Rows
        result = asList(createRow(concept, 0), createRow(concept, 0));
        assertThat(strategy.verifyRows(concept, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();
        assertThat(strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();

        result = asList(createRow(concept, 0), createRow(concept, 1));
        assertThat(strategy.verifyRows(concept, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();
        assertThat(strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();

        result = asList(createRow(concept, 1), createRow(concept, 1));
        assertThat(strategy.verifyRows(concept, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();
        assertThat(strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();
    }

    @Test
    void minMax() throws RuleException {
        AggregationVerification aggregationVerification = AggregationVerification.builder()
            .min(1)
            .max(1)
            .build();

        // Columns
        assertThat(strategy.verifyColumns(concept, aggregationVerification, COLUMN_NAMES, COLUMNS)
            .isSuccess()).isTrue();
        assertThat(strategy.verifyColumns(constraint, aggregationVerification, COLUMN_NAMES, COLUMNS)
            .isSuccess()).isTrue();

        // Rows
        result = asList(createRow(concept, 0), createRow(concept, 0));
        assertThat(strategy.verifyRows(concept, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();
        assertThat(strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();

        result = asList(createRow(concept, 0), createRow(concept, 1));
        assertThat(strategy.verifyRows(concept, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();
        assertThat(strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();

        result = asList(createRow(concept, 1), createRow(concept, 1));
        assertThat(strategy.verifyRows(concept, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();
        assertThat(strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();

        result = asList(createRow(concept, 1), createRow(concept, 2));
        assertThat(strategy.verifyRows(concept, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();
        assertThat(strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();

        result = asList(createRow(concept, 2), createRow(concept, 2));
        assertThat(strategy.verifyRows(concept, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();
        assertThat(strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();
    }

    @Test
    void explicitPrimaryColumn() throws RuleException {
        AggregationVerification aggregationVerification = AggregationVerification.builder()
            .column("c1")
            .build();
        result = asList(createRow(concept, 0, 1), createRow(concept, 0, 1));
        assertThat(strategy.verifyRows(concept, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();
        assertThat(strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();
    }

    @Test
    void unknownPrimaryColumn() {
        AggregationVerification aggregationVerification = AggregationVerification.builder()
            .column("cx")
            .build();
        result = asList(createRow(concept, 0, 1), createRow(concept, 0, 1));
        assertThatExceptionOfType(RuleException.class).isThrownBy(() -> strategy.verifyRows(concept, aggregationVerification, COLUMN_NAMES, result));
        assertThatExceptionOfType(RuleException.class).isThrownBy(() -> strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result));
    }

    @Test
    void emptyResult() throws RuleException {
        AggregationVerification aggregationVerification = AggregationVerification.builder()
            .build();
        result = Collections.emptyList();
        assertThat(strategy.verifyRows(concept, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();
        assertThat(strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();
    }

    private Row createRow(ExecutableRule<?> rule, int... values) {
        Map<String, Column<?>> columns = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i++) {
            columns.put("c" + i, toColumn(values[i]));
        }
        return toRow(rule, columns, FAILURE, Optional.empty());
    }
}
