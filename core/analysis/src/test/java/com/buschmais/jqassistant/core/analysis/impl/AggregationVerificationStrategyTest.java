package com.buschmais.jqassistant.core.analysis.impl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.report.api.configuration.Report;
import com.buschmais.jqassistant.core.report.api.model.Column;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.rule.api.model.*;
import com.buschmais.jqassistant.core.rule.api.reader.AggregationVerification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.buschmais.jqassistant.core.report.api.ReportHelper.toColumn;
import static com.buschmais.jqassistant.core.report.api.ReportHelper.toRow;
import static com.buschmais.jqassistant.core.report.api.model.Result.Status.*;
import static com.buschmais.jqassistant.core.rule.api.model.Severity.MAJOR;
import static com.buschmais.jqassistant.core.rule.api.model.Severity.MINOR;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class AggregationVerificationStrategyTest {

    static final List<String> COLUMN_NAMES = asList("c0", "c1");

    @Mock
    private Report configuration;

    @Mock
    private Concept concept;

    @Mock
    private Constraint constraint;

    private List<Row> result;

    private AggregationVerificationStrategy strategy;

    @BeforeEach
    void setUp() throws RuleException {
        doReturn(Severity.MINOR.name()).when(configuration)
            .warnOnSeverity();
        doReturn(Severity.MAJOR.name()).when(configuration)
            .failOnSeverity();
        strategy = new AggregationVerificationStrategy(configuration);
    }

    @Test
    void defaultConcept() throws RuleException {
        AggregationVerification aggregationVerification = AggregationVerification.builder()
            .build();
        result = asList(createRow(concept, 0), createRow(concept, 0));
        assertThat(strategy.verify(concept, MINOR, aggregationVerification, COLUMN_NAMES, result)).isEqualTo(WARNING);
        assertThat(strategy.verify(constraint, MAJOR, aggregationVerification, COLUMN_NAMES, result)).isEqualTo(SUCCESS);
        result = asList(createRow(concept, 0), createRow(concept, 1));
        assertThat(strategy.verify(concept, MINOR, aggregationVerification, COLUMN_NAMES, result)).isEqualTo(SUCCESS);
        assertThat(strategy.verify(constraint, MAJOR, aggregationVerification, COLUMN_NAMES, result)).isEqualTo(FAILURE);
        result = asList(createRow(concept, 1), createRow(concept, 1));
        assertThat(strategy.verify(concept, MINOR, aggregationVerification, COLUMN_NAMES, result)).isEqualTo(SUCCESS);
        assertThat(strategy.verify(constraint, MAJOR, aggregationVerification, COLUMN_NAMES, result)).isEqualTo(FAILURE);
    }

    @Test
    void min() throws RuleException {
        AggregationVerification aggregationVerification = AggregationVerification.builder()
            .min(1)
            .build();
        result = asList(createRow(concept, 0), createRow(concept, 0));
        assertThat(strategy.verify(concept, MINOR, aggregationVerification, COLUMN_NAMES, result)).isEqualTo(WARNING);
        assertThat(strategy.verify(constraint, MAJOR, aggregationVerification, COLUMN_NAMES, result)).isEqualTo(FAILURE);
        result = asList(createRow(concept, 0), createRow(concept, 1));
        assertThat(strategy.verify(concept, MINOR, aggregationVerification, COLUMN_NAMES, result)).isEqualTo(SUCCESS);
        assertThat(strategy.verify(constraint, MAJOR, aggregationVerification, COLUMN_NAMES, result)).isEqualTo(SUCCESS);
        result = asList(createRow(concept, 1), createRow(concept, 1));
        assertThat(strategy.verify(concept, MINOR, aggregationVerification, COLUMN_NAMES, result)).isEqualTo(SUCCESS);
        assertThat(strategy.verify(constraint, MAJOR, aggregationVerification, COLUMN_NAMES, result)).isEqualTo(SUCCESS);
    }

    @Test
    void max() throws RuleException {
        AggregationVerification aggregationVerification = AggregationVerification.builder()
            .max(0)
            .build();
        result = asList(createRow(concept, 0), createRow(concept, 0));
        assertThat(strategy.verify(concept, MINOR, aggregationVerification, COLUMN_NAMES, result)).isEqualTo(SUCCESS);
        assertThat(strategy.verify(constraint, MAJOR, aggregationVerification, COLUMN_NAMES, result)).isEqualTo(SUCCESS);
        result = asList(createRow(concept, 0), createRow(concept, 1));
        assertThat(strategy.verify(concept, MINOR, aggregationVerification, COLUMN_NAMES, result)).isEqualTo(WARNING);
        assertThat(strategy.verify(constraint, MAJOR, aggregationVerification, COLUMN_NAMES, result)).isEqualTo(FAILURE);
        result = asList(createRow(concept, 1), createRow(concept, 1));
        assertThat(strategy.verify(concept, MINOR, aggregationVerification, COLUMN_NAMES, result)).isEqualTo(WARNING);
        assertThat(strategy.verify(constraint, MAJOR, aggregationVerification, COLUMN_NAMES, result)).isEqualTo(FAILURE);
    }

    @Test
    void minMax() throws RuleException {
        AggregationVerification aggregationVerification = AggregationVerification.builder()
            .min(1)
            .max(1)
            .build();
        result = asList(createRow(concept, 0), createRow(concept, 0));
        assertThat(strategy.verify(concept, MINOR, aggregationVerification, COLUMN_NAMES, result)).isEqualTo(WARNING);
        assertThat(strategy.verify(constraint, MAJOR, aggregationVerification, COLUMN_NAMES, result)).isEqualTo(FAILURE);
        result = asList(createRow(concept, 0), createRow(concept, 1));
        assertThat(strategy.verify(concept, MINOR, aggregationVerification, COLUMN_NAMES, result)).isEqualTo(SUCCESS);
        assertThat(strategy.verify(constraint, MAJOR, aggregationVerification, COLUMN_NAMES, result)).isEqualTo(SUCCESS);
        result = asList(createRow(concept, 1), createRow(concept, 1));
        assertThat(strategy.verify(concept, MINOR, aggregationVerification, COLUMN_NAMES, result)).isEqualTo(WARNING);
        assertThat(strategy.verify(constraint, MAJOR, aggregationVerification, COLUMN_NAMES, result)).isEqualTo(FAILURE);
        result = asList(createRow(concept, 1), createRow(concept, 2));
        assertThat(strategy.verify(concept, MINOR, aggregationVerification, COLUMN_NAMES, result)).isEqualTo(WARNING);
        assertThat(strategy.verify(constraint, MAJOR, aggregationVerification, COLUMN_NAMES, result)).isEqualTo(FAILURE);
        result = asList(createRow(concept, 2), createRow(concept, 2));
        assertThat(strategy.verify(concept, MINOR, aggregationVerification, COLUMN_NAMES, result)).isEqualTo(WARNING);
        assertThat(strategy.verify(constraint, MAJOR, aggregationVerification, COLUMN_NAMES, result)).isEqualTo(FAILURE);
    }

    @Test
    void colum() throws RuleException {
        AggregationVerification aggregationVerification = AggregationVerification.builder()
            .column("c1")
            .build();
        result = asList(createRow(concept, 0, 1), createRow(concept, 0, 1));
        assertThat(strategy.verify(concept, MINOR, aggregationVerification, COLUMN_NAMES, result)).isEqualTo(SUCCESS);
        assertThat(strategy.verify(constraint, MAJOR, aggregationVerification, COLUMN_NAMES, result)).isEqualTo(FAILURE);
    }

    @Test
    void emptyResult() throws RuleException {
        AggregationVerification aggregationVerification = AggregationVerification.builder()
            .build();
        result = Collections.emptyList();
        assertThat(strategy.verify(concept, MAJOR, aggregationVerification, COLUMN_NAMES, result)).isEqualTo(FAILURE);
        assertThat(strategy.verify(constraint, MAJOR, aggregationVerification, COLUMN_NAMES, result)).isEqualTo(SUCCESS);
    }

    private Row createRow(ExecutableRule<?> rule, int... values) {
        Map<String, Column<?>> columns = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i++) {
            columns.put("c" + i, toColumn(values[i]));
        }
        return toRow(rule, columns);
    }
}
