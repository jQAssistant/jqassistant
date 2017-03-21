package com.buschmais.jqassistant.core.analysis.impl;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.FAILURE;
import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.rule.api.executor.RuleExecutorException;
import com.buschmais.jqassistant.core.rule.api.reader.AggregationVerification;

@RunWith(MockitoJUnitRunner.class)
public class AggregationVerificationStrategyTest {

    public static final List<String> COLUMN_NAMES = asList("a");

    @Mock
    private Concept concept;

    @Mock
    private Constraint constraint;

    private List<Map<String, Object>> result;

    private AggregationVerificationStrategy strategy = new AggregationVerificationStrategy();

    @Test
    public void defaultConcept() throws RuleExecutorException {
        AggregationVerification aggregationVerification = AggregationVerification.builder().build();
        result = asList(createRow(0),createRow(0));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        result = asList(createRow(0),createRow(1));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        result = asList(createRow(1),createRow(1));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
    }

    @Test
    public void min() throws RuleExecutorException {
        AggregationVerification aggregationVerification = AggregationVerification.builder().min(1).build();
        result = asList(createRow(0),createRow(0));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        result = asList(createRow(0),createRow(1));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        result = asList(createRow(1),createRow(1));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
    }

    @Test
    public void max() throws RuleExecutorException {
        AggregationVerification aggregationVerification = AggregationVerification.builder().max(0).build();
        result = asList(createRow(0),createRow(0));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        result = asList(createRow(0),createRow(1));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        result = asList(createRow(1),createRow(1));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
    }

    @Test
    public void minMax() throws RuleExecutorException {
        AggregationVerification aggregationVerification = AggregationVerification.builder().min(1).max(1).build();
        result = asList(createRow(0),createRow(0));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        result = asList(createRow(0),createRow(1));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        result = asList(createRow(1),createRow(1));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        result = asList(createRow(1),createRow(2));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        result = asList(createRow(2),createRow(2));
        assertThat(strategy.verify(concept, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        assertThat(strategy.verify(constraint, aggregationVerification, COLUMN_NAMES, result), equalTo(FAILURE));
    }

    private Map<String, Object> createRow(int value) {
        HashMap<String, Object> row = new HashMap<>();
        row.put("a", value);
        return row;
    }
}
