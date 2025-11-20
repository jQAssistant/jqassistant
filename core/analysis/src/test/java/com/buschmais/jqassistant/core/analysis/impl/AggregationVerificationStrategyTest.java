package com.buschmais.jqassistant.core.analysis.impl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(MockitoExtension.class)
class AggregationVerificationStrategyTest {

    static final List<String> COLUMN_NAMES = asList("c0", "c1");

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
    void defaultConcept() throws RuleException {
        AggregationVerification aggregationVerification = AggregationVerification.builder()
            .build();

        result = asList(createRow(concept, 0), createRow(concept, 0));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();

        result = asList(createRow(concept, 0), createRow(concept, 1));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();

        result = asList(createRow(concept, 1), createRow(concept, 1));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();
    }

    @Test
    void min() throws RuleException {
        AggregationVerification aggregationVerification = AggregationVerification.builder()
            .min(1)
            .build();

        result = asList(createRow(concept, 0), createRow(concept, 0));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();

        result = asList(createRow(concept, 0), createRow(concept, 1));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();

        result = asList(createRow(concept, 1), createRow(concept, 1));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();
    }

    @Test
    void max() throws RuleException {
        AggregationVerification aggregationVerification = AggregationVerification.builder()
            .max(0)
            .build();

        result = asList(createRow(concept, 0), createRow(concept, 0));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();

        result = asList(createRow(concept, 0), createRow(concept, 1));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();

        result = asList(createRow(concept, 1), createRow(concept, 1));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();
    }

    @Test
    void minMax() throws RuleException {
        AggregationVerification aggregationVerification = AggregationVerification.builder()
            .min(1)
            .max(1)
            .build();

        result = asList(createRow(concept, 0), createRow(concept, 0));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();

        result = asList(createRow(concept, 0), createRow(concept, 1));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();

        result = asList(createRow(concept, 1), createRow(concept, 1));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();

        result = asList(createRow(concept, 1), createRow(concept, 2));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();

        result = asList(createRow(concept, 2), createRow(concept, 2));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();
    }

    @Test
    void colum() throws RuleException {
        AggregationVerification aggregationVerification = AggregationVerification.builder()
            .column("c1")
            .build();
        result = asList(createRow(concept, 0, 1), createRow(concept, 0, 1));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();
    }

    @Test
    void unknownColumn() {
        AggregationVerification aggregationVerification = AggregationVerification.builder()
            .column("cx")
            .build();
        result = asList(createRow(concept, 0, 1), createRow(concept, 0, 1));
        assertThatExceptionOfType(RuleException.class).isThrownBy(() -> strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result));
        assertThatExceptionOfType(RuleException.class).isThrownBy(() -> strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result));
    }

    @Test
    void emptyResult() throws RuleException {
        AggregationVerification aggregationVerification = AggregationVerification.builder()
            .build();
        result = Collections.emptyList();
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isFalse();
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result)
            .isSuccess()).isTrue();
    }

    private Row createRow(ExecutableRule<?> rule, int... values) {
        Map<String, Column<?>> columns = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i++) {
            columns.put("c" + i, toColumn(values[i]));
        }
        return toRow(rule, columns, null);
    }
}
