package com.buschmais.jqassistant.core.analysis.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.buschmais.jqassistant.core.report.api.model.Column;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.report.api.model.VerificationResult;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.Hidden;
import com.buschmais.jqassistant.core.rule.api.reader.RowCountVerification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RowCountVerificationStrategyTest {

    static final List<String> COLUMN_NAMES = singletonList("a");

    static final Map<String, Column<?>> COLUMNS = Map.of("a", Column.builder()
        .value("hello")
        .label("hello")
        .sourceLocation(empty())
        .build());

    @Mock
    private Concept concept;

    @Mock
    private Constraint constraint;

    private RowCountVerificationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new RowCountVerificationStrategy();
    }

    @Test
    void defaultConfiguration() {
        RowCountVerification rowCountVerification = RowCountVerification.builder()
            .build();

        // Columns
        assertThat(strategy.verifyColumns(concept, rowCountVerification, COLUMN_NAMES, COLUMNS)
            .isSuccess()).isTrue();
        assertThat(strategy.verifyColumns(constraint, rowCountVerification, COLUMN_NAMES, COLUMNS)
            .isSuccess()).isFalse();

        // Rows
        List<Row> result1 = createRows(0, 0);
        assertThat(strategy.verifyRows(concept, rowCountVerification, COLUMN_NAMES, result1)
            .isSuccess()).isFalse();
        assertThat(strategy.verifyRows(constraint, rowCountVerification, COLUMN_NAMES, result1)
            .isSuccess()).isTrue();

        List<Row> result2 = createRows(1, 0);
        assertThat(strategy.verifyRows(concept, rowCountVerification, COLUMN_NAMES, result2)
            .isSuccess()).isTrue();
        assertThat(strategy.verifyRows(constraint, rowCountVerification, COLUMN_NAMES, result2)
            .isSuccess()).isFalse();
    }

    @Test
    void min() {
        RowCountVerification rowCountVerification = RowCountVerification.builder()
            .min(1)
            .build();

        // Columns
        assertThat(strategy.verifyColumns(concept, rowCountVerification, COLUMN_NAMES, COLUMNS)
            .isSuccess()).isTrue();
        assertThat(strategy.verifyColumns(constraint, rowCountVerification, COLUMN_NAMES, COLUMNS)
            .isSuccess()).isTrue();

        // Rows
        List<Row> result1 = createRows(0, 0);
        assertThat(strategy.verifyRows(concept, rowCountVerification, COLUMN_NAMES, result1)
            .isSuccess()).isFalse();
        assertThat(strategy.verifyRows(constraint, rowCountVerification, COLUMN_NAMES, result1)
            .isSuccess()).isFalse();

        List<Row> result2 = createRows(1, 0);
        assertThat(strategy.verifyRows(concept, rowCountVerification, COLUMN_NAMES, result2)
            .isSuccess()).isTrue();
        assertThat(strategy.verifyRows(constraint, rowCountVerification, COLUMN_NAMES, result2)
            .isSuccess()).isTrue();
    }

    @Test
    void max() {
        RowCountVerification rowCountVerification = RowCountVerification.builder()
            .max(0)
            .build();

        // Columns
        assertThat(strategy.verifyColumns(concept, rowCountVerification, COLUMN_NAMES, COLUMNS)
            .isSuccess()).isFalse();
        assertThat(strategy.verifyColumns(constraint, rowCountVerification, COLUMN_NAMES, COLUMNS)
            .isSuccess()).isFalse();

        // Rows
        List<Row> result1 = createRows(0, 0);
        assertThat(strategy.verifyRows(concept, rowCountVerification, COLUMN_NAMES, result1)
            .isSuccess()).isTrue();
        assertThat(strategy.verifyRows(constraint, rowCountVerification, COLUMN_NAMES, result1)
            .isSuccess()).isTrue();

        List<Row> result2 = createRows(1, 0);
        assertThat(strategy.verifyRows(concept, rowCountVerification, COLUMN_NAMES, result2)
            .isSuccess()).isFalse();
        assertThat(strategy.verifyRows(constraint, rowCountVerification, COLUMN_NAMES, result2)
            .isSuccess()).isFalse();
    }

    @Test
    void minMax() {
        RowCountVerification rowCountVerification = RowCountVerification.builder()
            .min(1)
            .max(1)
            .build();

        // Columns
        assertThat(strategy.verifyColumns(concept, rowCountVerification, COLUMN_NAMES, COLUMNS)
            .isSuccess()).isTrue();
        assertThat(strategy.verifyColumns(constraint, rowCountVerification, COLUMN_NAMES, COLUMNS)
            .isSuccess()).isTrue();

        // Rows
        List<Row> result1 = createRows(0, 0);
        assertThat(strategy.verifyRows(concept, rowCountVerification, COLUMN_NAMES, result1)
            .isSuccess()).isFalse();
        assertThat(strategy.verifyRows(constraint, rowCountVerification, COLUMN_NAMES, result1)
            .isSuccess()).isFalse();

        List<Row> result2 = createRows(1, 0);
        assertThat(strategy.verifyRows(concept, rowCountVerification, COLUMN_NAMES, result2)
            .isSuccess()).isTrue();
        assertThat(strategy.verifyRows(constraint, rowCountVerification, COLUMN_NAMES, result2)
            .isSuccess()).isTrue();

        List<Row> result3 = createRows(2, 0);
        assertThat(strategy.verifyRows(concept, rowCountVerification, COLUMN_NAMES, result3)
            .isSuccess()).isFalse();
        assertThat(strategy.verifyRows(constraint, rowCountVerification, COLUMN_NAMES, result3)
            .isSuccess()).isFalse();
    }

    @Test
    void hiddenRows() {
        RowCountVerification rowCountVerification = RowCountVerification.builder()
            .max(1)
            .build();

        List<Row> result = createRows(1, 10);
        VerificationResult verificationResult = strategy.verifyRows(constraint, rowCountVerification, COLUMN_NAMES, result);

        assertThat(verificationResult.isSuccess()).isTrue();
        assertThat(verificationResult.getRowCount()).isEqualTo(1);
        assertThat(verificationResult.getHiddenRowCount()).isEqualTo(10);
    }

    private List<Row> createRows(int rowCount, int hiddenRowCount) {
        List<Row> rows = new ArrayList<>();
        for (int i = 0; i < rowCount; i++) {
            rows.add(createRow(i, false));
        }
        for (int i = rowCount; i < rowCount + hiddenRowCount; i++) {
            rows.add(createRow(i, true));
        }
        return rows;
    }

    private static Row createRow(int i, boolean hidden) {
        return Row.builder()
            .key(Integer.toString(i))
            .columns(Map.of("A", Column.builder()
                .value("1")
                .label("1")
                .sourceLocation(empty())
                .build()))
            .status(SUCCESS)
            .hidden(hidden ?
                Optional.of(Hidden.builder()
                    .suppression(Hidden.Suppression.builder()
                        .build())
                    .build()) :
                empty())
            .build();
    }
}
