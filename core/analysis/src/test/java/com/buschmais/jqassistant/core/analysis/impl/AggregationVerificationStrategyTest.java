package com.buschmais.jqassistant.core.analysis.impl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.reader.AggregationVerification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.FAILURE;
import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@ExtendWith(MockitoExtension.class)
public class AggregationVerificationStrategyTest {

    public static final List<String> COLUMN_NAMES = asList("c0", "c1");

    @Mock
    private Concept concept;

    @Mock
    private Constraint constraint;

    private List<Map<String, Object>> result;

    private AggregationVerificationStrategy strategy = new AggregationVerificationStrategy();

    @Test
    public void defaultConcept() throws RuleException {
        AggregationVerification aggregationVerification = AggregationVerification.builder().build();
        result = asList(createRow(0), createRow(0));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        result = asList(createRow(0), createRow(1));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        result = asList(createRow(1), createRow(1));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
    }

    @Test
    public void min() throws RuleException {
        AggregationVerification aggregationVerification = AggregationVerification.builder().min(1).build();
        result = asList(createRow(0), createRow(0));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        result = asList(createRow(0), createRow(1));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        result = asList(createRow(1), createRow(1));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
    }

    @Test
    public void max() throws RuleException {
        AggregationVerification aggregationVerification = AggregationVerification.builder().max(0).build();
        result = asList(createRow(0), createRow(0));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        result = asList(createRow(0), createRow(1));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        result = asList(createRow(1), createRow(1));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
    }

    @Test
    public void minMax() throws RuleException {
        AggregationVerification aggregationVerification = AggregationVerification.builder().min(1).max(1).build();
        result = asList(createRow(0), createRow(0));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        result = asList(createRow(0), createRow(1));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        result = asList(createRow(1), createRow(1));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        result = asList(createRow(1), createRow(2));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        result = asList(createRow(2), createRow(2));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
    }

    @Test
    public void colum() throws RuleException {
        AggregationVerification aggregationVerification = AggregationVerification.builder().column("c1").build();
        result = asList(createRow(0, 1), createRow(0, 1));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
    }

    @Test
    public void emptyResult() throws RuleException {
        AggregationVerification aggregationVerification = AggregationVerification.builder().build();
        result = Collections.emptyList();
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
    }

    private Map<String, Object> createRow(int... values) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i++) {
            row.put("c" + i, values[i]);
        }
        return row;
    }
}
