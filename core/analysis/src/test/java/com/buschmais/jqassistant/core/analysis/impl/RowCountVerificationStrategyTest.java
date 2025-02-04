package com.buschmais.jqassistant.core.analysis.impl;

import java.util.List;

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
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RowCountVerificationStrategyTest {

    static final List<String> COLUMN_NAMES = singletonList("a");

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

        when(result.size()).thenReturn(0);
        assertThat(strategy.verify(concept, rowCountVerification, COLUMN_NAMES, result)
            .isSuccessful()).isFalse();
        assertThat(strategy.verify(constraint, rowCountVerification, COLUMN_NAMES, result)
            .isSuccessful()).isTrue();

        when(result.size()).thenReturn(1);
        assertThat(strategy.verify(concept, rowCountVerification, COLUMN_NAMES, result)
            .isSuccessful()).isTrue();
        assertThat(strategy.verify(constraint, rowCountVerification, COLUMN_NAMES, result)
            .isSuccessful()).isFalse();
    }

    @Test
    void min() {
        RowCountVerification rowCountVerification = RowCountVerification.builder()
            .min(1)
            .build();

        when(result.size()).thenReturn(0);
        assertThat(strategy.verify(concept, rowCountVerification, COLUMN_NAMES, result)
            .isSuccessful()).isFalse();
        assertThat(strategy.verify(constraint, rowCountVerification, COLUMN_NAMES, result)
            .isSuccessful()).isFalse();

        when(result.size()).thenReturn(1);
        assertThat(strategy.verify(concept, rowCountVerification, COLUMN_NAMES, result)
            .isSuccessful()).isTrue();
        assertThat(strategy.verify(constraint, rowCountVerification, COLUMN_NAMES, result)
            .isSuccessful()).isTrue();
    }

    @Test
    void max() {
        RowCountVerification rowCountVerification = RowCountVerification.builder()
            .max(0)
            .build();

        when(result.size()).thenReturn(0);
        assertThat(strategy.verify(concept, rowCountVerification, COLUMN_NAMES, result)
            .isSuccessful()).isTrue();
        assertThat(strategy.verify(constraint, rowCountVerification, COLUMN_NAMES, result)
            .isSuccessful()).isTrue();

        when(result.size()).thenReturn(1);
        assertThat(strategy.verify(concept, rowCountVerification, COLUMN_NAMES, result)
            .isSuccessful()).isFalse();
        assertThat(strategy.verify(constraint, rowCountVerification, COLUMN_NAMES, result)
            .isSuccessful()).isFalse();
    }

    @Test
    void minMax() {
        RowCountVerification rowCountVerification = RowCountVerification.builder()
            .min(1)
            .max(1)
            .build();

        when(result.size()).thenReturn(0);
        assertThat(strategy.verify(concept, rowCountVerification, COLUMN_NAMES, result)
            .isSuccessful()).isFalse();
        assertThat(strategy.verify(constraint, rowCountVerification, COLUMN_NAMES, result)
            .isSuccessful()).isFalse();

        when(result.size()).thenReturn(1);
        assertThat(strategy.verify(concept, rowCountVerification, COLUMN_NAMES, result)
            .isSuccessful()).isTrue();
        assertThat(strategy.verify(constraint, rowCountVerification, COLUMN_NAMES, result)
            .isSuccessful()).isTrue();

        when(result.size()).thenReturn(2);
        assertThat(strategy.verify(concept, rowCountVerification, COLUMN_NAMES, result)
            .isSuccessful()).isFalse();
        assertThat(strategy.verify(constraint, rowCountVerification, COLUMN_NAMES, result)
            .isSuccessful()).isFalse();
    }

}
