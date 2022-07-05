package com.buschmais.jqassistant.core.analysis.impl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.report.api.configuration.Report;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.core.rule.api.reader.AggregationVerification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.*;
import static com.buschmais.jqassistant.core.rule.api.model.Severity.MAJOR;
import static com.buschmais.jqassistant.core.rule.api.model.Severity.MINOR;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
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

    private List<Map<String, Object>> result;

    private AggregationVerificationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new AggregationVerificationStrategy(configuration);
        doReturn(Severity.Threshold.from(MAJOR)).when(configuration)
            .failOnSeverity();
        doReturn(Severity.Threshold.from(MINOR)).when(configuration)
            .warnOnSeverity();
    }

    @Test
    void defaultConcept() throws RuleException {
        AggregationVerification aggregationVerification = AggregationVerification.builder()
            .build();
        result = asList(createRow(0), createRow(0));
        assertThat(strategy.verify(concept, MINOR, aggregationVerification, COLUMN_NAMES, result), equalTo(WARNING));
        assertThat(strategy.verify(constraint, MAJOR, aggregationVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        result = asList(createRow(0), createRow(1));
        assertThat(strategy.verify(concept, MINOR, aggregationVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        assertThat(strategy.verify(constraint, MAJOR, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        result = asList(createRow(1), createRow(1));
        assertThat(strategy.verify(concept, MINOR, aggregationVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        assertThat(strategy.verify(constraint, MAJOR, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
    }

    @Test
    void min() throws RuleException {
        AggregationVerification aggregationVerification = AggregationVerification.builder()
            .min(1)
            .build();
        result = asList(createRow(0), createRow(0));
        assertThat(strategy.verify(concept, MINOR, aggregationVerification, COLUMN_NAMES, result), equalTo(WARNING));
        assertThat(strategy.verify(constraint, MAJOR, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        result = asList(createRow(0), createRow(1));
        assertThat(strategy.verify(concept, MINOR, aggregationVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        assertThat(strategy.verify(constraint, MAJOR, aggregationVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        result = asList(createRow(1), createRow(1));
        assertThat(strategy.verify(concept, MINOR, aggregationVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        assertThat(strategy.verify(constraint, MAJOR, aggregationVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
    }

    @Test
    void max() throws RuleException {
        AggregationVerification aggregationVerification = AggregationVerification.builder()
            .max(0)
            .build();
        result = asList(createRow(0), createRow(0));
        assertThat(strategy.verify(concept, MINOR, aggregationVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        assertThat(strategy.verify(constraint, MAJOR, aggregationVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        result = asList(createRow(0), createRow(1));
        assertThat(strategy.verify(concept, MINOR, aggregationVerification, COLUMN_NAMES, result), equalTo(WARNING));
        assertThat(strategy.verify(constraint, MAJOR, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        result = asList(createRow(1), createRow(1));
        assertThat(strategy.verify(concept, MINOR, aggregationVerification, COLUMN_NAMES, result), equalTo(WARNING));
        assertThat(strategy.verify(constraint, MAJOR, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
    }

    @Test
    void minMax() throws RuleException {
        AggregationVerification aggregationVerification = AggregationVerification.builder()
            .min(1)
            .max(1)
            .build();
        result = asList(createRow(0), createRow(0));
        assertThat(strategy.verify(concept, MINOR, aggregationVerification, COLUMN_NAMES, result), equalTo(WARNING));
        assertThat(strategy.verify(constraint, MAJOR, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        result = asList(createRow(0), createRow(1));
        assertThat(strategy.verify(concept, MINOR, aggregationVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        assertThat(strategy.verify(constraint, MAJOR, aggregationVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        result = asList(createRow(1), createRow(1));
        assertThat(strategy.verify(concept, MINOR, aggregationVerification, COLUMN_NAMES, result), equalTo(WARNING));
        assertThat(strategy.verify(constraint, MAJOR, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        result = asList(createRow(1), createRow(2));
        assertThat(strategy.verify(concept, MINOR, aggregationVerification, COLUMN_NAMES, result), equalTo(WARNING));
        assertThat(strategy.verify(constraint, MAJOR, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        result = asList(createRow(2), createRow(2));
        assertThat(strategy.verify(concept, MINOR, aggregationVerification, COLUMN_NAMES, result), equalTo(WARNING));
        assertThat(strategy.verify(constraint, MAJOR, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
    }

    @Test
    void colum() throws RuleException {
        AggregationVerification aggregationVerification = AggregationVerification.builder()
            .column("c1")
            .build();
        result = asList(createRow(0, 1), createRow(0, 1));
        assertThat(strategy.verify(concept, MINOR, aggregationVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        assertThat(strategy.verify(constraint, MAJOR, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
    }

    @Test
    void emptyResult() throws RuleException {
        AggregationVerification aggregationVerification = AggregationVerification.builder()
            .build();
        result = Collections.emptyList();
        assertThat(strategy.verify(concept, MAJOR, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        assertThat(strategy.verify(constraint, MAJOR, aggregationVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
    }

    private Map<String, Object> createRow(int... values) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i++) {
            row.put("c" + i, values[i]);
        }
        return row;
    }
}
