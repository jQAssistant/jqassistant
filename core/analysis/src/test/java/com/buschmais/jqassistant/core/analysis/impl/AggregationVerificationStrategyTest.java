package com.buschmais.jqassistant.core.analysis.impl;

import java.util.*;

import com.buschmais.jqassistant.core.report.api.model.Column;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.report.api.model.VerificationResult;
import com.buschmais.jqassistant.core.rule.api.model.*;
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
        List<Row> result1 = asList(createRow(concept, false, 0), createRow(concept, false, 0));
        assertThat(strategy.verifyRows(concept, aggregationVerification, COLUMN_NAMES, result1)
            .isSuccess()).isFalse();
        assertThat(strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result1)
            .isSuccess()).isTrue();

        List<Row> result2 = asList(createRow(concept, false, 0), createRow(concept, false, 1));
        assertThat(strategy.verifyRows(concept, aggregationVerification, COLUMN_NAMES, result2)
            .isSuccess()).isTrue();
        assertThat(strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result2)
            .isSuccess()).isFalse();

        List<Row> result3 = asList(createRow(concept, false, 1), createRow(concept, false, 1));
        assertThat(strategy.verifyRows(concept, aggregationVerification, COLUMN_NAMES, result3)
            .isSuccess()).isTrue();
        assertThat(strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result3)
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
        List<Row> result1 = asList(createRow(concept, false, 0), createRow(concept, false, 0));
        assertThat(strategy.verifyRows(concept, aggregationVerification, COLUMN_NAMES, result1)
            .isSuccess()).isFalse();
        assertThat(strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result1)
            .isSuccess()).isFalse();

        List<Row> result2 = asList(createRow(concept, false, 0), createRow(concept, false, 1));
        assertThat(strategy.verifyRows(concept, aggregationVerification, COLUMN_NAMES, result2)
            .isSuccess()).isTrue();
        assertThat(strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result2)
            .isSuccess()).isTrue();

        List<Row> result3 = asList(createRow(concept, false, 1), createRow(concept, false, 1));
        assertThat(strategy.verifyRows(concept, aggregationVerification, COLUMN_NAMES, result3)
            .isSuccess()).isTrue();
        assertThat(strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result3)
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
        List<Row> result1 = asList(createRow(concept, false, 0), createRow(concept, false, 0));
        assertThat(strategy.verifyRows(concept, aggregationVerification, COLUMN_NAMES, result1)
            .isSuccess()).isTrue();
        assertThat(strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result1)
            .isSuccess()).isTrue();

        List<Row> result2 = asList(createRow(concept, false, 0), createRow(concept, false, 1));
        assertThat(strategy.verifyRows(concept, aggregationVerification, COLUMN_NAMES, result2)
            .isSuccess()).isFalse();
        assertThat(strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result2)
            .isSuccess()).isFalse();

        List<Row> result3 = asList(createRow(concept, false, 1), createRow(concept, false, 1));
        assertThat(strategy.verifyRows(concept, aggregationVerification, COLUMN_NAMES, result3)
            .isSuccess()).isFalse();
        assertThat(strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result3)
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
        List<Row> result1 = asList(createRow(concept, false, 0), createRow(concept, false, 0));
        assertThat(strategy.verifyRows(concept, aggregationVerification, COLUMN_NAMES, result1)
            .isSuccess()).isFalse();
        assertThat(strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result1)
            .isSuccess()).isFalse();

        List<Row> result2 = asList(createRow(concept, false, 0), createRow(concept, false, 1));
        assertThat(strategy.verifyRows(concept, aggregationVerification, COLUMN_NAMES, result2)
            .isSuccess()).isTrue();
        assertThat(strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result2)
            .isSuccess()).isTrue();

        List<Row> result3 = asList(createRow(concept, false, 1), createRow(concept, false, 1));
        assertThat(strategy.verifyRows(concept, aggregationVerification, COLUMN_NAMES, result3)
            .isSuccess()).isFalse();
        assertThat(strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result3)
            .isSuccess()).isFalse();

        List<Row> result4 = asList(createRow(concept, false, 1), createRow(concept, false, 2));
        assertThat(strategy.verifyRows(concept, aggregationVerification, COLUMN_NAMES, result4)
            .isSuccess()).isFalse();
        assertThat(strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result4)
            .isSuccess()).isFalse();

        List<Row> result5 = asList(createRow(concept, false, 2), createRow(concept, false, 2));
        assertThat(strategy.verifyRows(concept, aggregationVerification, COLUMN_NAMES, result5)
            .isSuccess()).isFalse();
        assertThat(strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result5)
            .isSuccess()).isFalse();
    }

    @Test
    void explicitPrimaryColumn() throws RuleException {
        AggregationVerification aggregationVerification = AggregationVerification.builder()
            .column("c1")
            .build();
        List<Row> result = asList(createRow(concept, false, 0, 1), createRow(concept, false, 0, 1));
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
        List<Row> result = asList(createRow(concept, false, 0, 1), createRow(concept, false, 0, 1));
        assertThatExceptionOfType(RuleException.class).isThrownBy(() -> strategy.verifyRows(concept, aggregationVerification, COLUMN_NAMES, result));
        assertThatExceptionOfType(RuleException.class).isThrownBy(() -> strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result));
    }

    @Test
    void emptyResult() throws RuleException {
        AggregationVerification aggregationVerification = AggregationVerification.builder()
            .build();
        List<Row> result = Collections.emptyList();
        assertThat(strategy.verifyRows(concept, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();
        assertThat(strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();
    }

    @Test
    void hiddenRows() throws RuleException {
        AggregationVerification aggregationVerification = AggregationVerification.builder()
            .max(1)
            .build();
        List<Row> result = asList(createRow(constraint, false, 1), createRow(constraint, true, 5), createRow(constraint, true, 5));

        VerificationResult verificationResult = strategy.verifyRows(constraint, aggregationVerification, COLUMN_NAMES, result);

        assertThat(verificationResult.isSuccess()).isTrue();
        assertThat(verificationResult.getRowCount()).isEqualTo(1);
        assertThat(verificationResult.getHiddenRowCount()).isEqualTo(10);
    }

    private Row createRow(ExecutableRule<?> rule, boolean hidden, int... values) {
        Map<String, Column<?>> columns = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i++) {
            columns.put("c" + i, toColumn(values[i]));
        }
        return toRow(rule, columns, FAILURE, hidden ?
            Optional.of(Hidden.builder()
                .suppression(Hidden.Suppression.builder()
                    .build())
                .build()) :
            empty());
    }
}
