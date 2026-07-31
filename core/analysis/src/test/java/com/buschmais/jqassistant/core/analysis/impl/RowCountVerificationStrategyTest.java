package com.buschmais.jqassistant.core.analysis.impl;

import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.report.api.model.Column;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.reader.RowCountVerification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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

    @Mock
    private List<Row> result;

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
        when(result.size()).thenReturn(0);
        assertThat(strategy.verifyRows(concept, rowCountVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();
        assertThat(strategy.verifyRows(constraint, rowCountVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();

        when(result.size()).thenReturn(1);
        assertThat(strategy.verifyRows(concept, rowCountVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();
        assertThat(strategy.verifyRows(constraint, rowCountVerification, COLUMN_NAMES, result)
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
        when(result.size()).thenReturn(0);
        assertThat(strategy.verifyRows(concept, rowCountVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();
        assertThat(strategy.verifyRows(constraint, rowCountVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();

        when(result.size()).thenReturn(1);
        assertThat(strategy.verifyRows(concept, rowCountVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();
        assertThat(strategy.verifyRows(constraint, rowCountVerification, COLUMN_NAMES, result)
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
        when(result.size()).thenReturn(0);
        assertThat(strategy.verifyRows(concept, rowCountVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();
        assertThat(strategy.verifyRows(constraint, rowCountVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();

        when(result.size()).thenReturn(1);
        assertThat(strategy.verifyRows(concept, rowCountVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();
        assertThat(strategy.verifyRows(constraint, rowCountVerification, COLUMN_NAMES, result)
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
        when(result.size()).thenReturn(0);
        assertThat(strategy.verifyRows(concept, rowCountVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();
        assertThat(strategy.verifyRows(constraint, rowCountVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();

        when(result.size()).thenReturn(1);
        assertThat(strategy.verifyRows(concept, rowCountVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();
        assertThat(strategy.verifyRows(constraint, rowCountVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();

        when(result.size()).thenReturn(2);
        assertThat(strategy.verifyRows(concept, rowCountVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();
        assertThat(strategy.verifyRows(constraint, rowCountVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();
    }

}
