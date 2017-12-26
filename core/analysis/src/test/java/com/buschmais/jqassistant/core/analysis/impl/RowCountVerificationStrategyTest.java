package com.buschmais.jqassistant.core.analysis.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.rule.api.reader.RowCountVerification;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.FAILURE;
import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RowCountVerificationStrategyTest {

    public static final List<String> COLUMN_NAMES = Arrays.asList("a");

    @Mock
    private Concept concept;

    @Mock
    private Constraint constraint;

    @Mock
    private List<Map<String, Object>> result;

    private RowCountVerificationStrategy strategy = new RowCountVerificationStrategy();

    @Test
    public void defaultConfiguration() throws RuleException {
        RowCountVerification rowCountVerification = RowCountVerification.builder().build();
        when(result.size()).thenReturn(0);
        assertThat(strategy.verify(concept, rowCountVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        assertThat(strategy.verify(constraint, rowCountVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        when(result.size()).thenReturn(1);
        assertThat(strategy.verify(concept, rowCountVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        assertThat(strategy.verify(constraint, rowCountVerification, COLUMN_NAMES, result), equalTo(FAILURE));
    }

    @Test
    public void min() throws RuleException {
        RowCountVerification rowCountVerification = RowCountVerification.builder().min(1).build();
        when(result.size()).thenReturn(0);
        assertThat(strategy.verify(concept, rowCountVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        assertThat(strategy.verify(constraint, rowCountVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        when(result.size()).thenReturn(1);
        assertThat(strategy.verify(concept, rowCountVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        assertThat(strategy.verify(constraint, rowCountVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
    }

    @Test
    public void max() throws RuleException {
        RowCountVerification rowCountVerification = RowCountVerification.builder().max(0).build();
        when(result.size()).thenReturn(0);
        assertThat(strategy.verify(concept, rowCountVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        assertThat(strategy.verify(constraint, rowCountVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        when(result.size()).thenReturn(1);
        assertThat(strategy.verify(concept, rowCountVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        assertThat(strategy.verify(constraint, rowCountVerification, COLUMN_NAMES, result), equalTo(FAILURE));
    }

    @Test
    public void minMax() throws RuleException {
        RowCountVerification rowCountVerification = RowCountVerification.builder().min(1).max(1).build();
        when(result.size()).thenReturn(0);
        assertThat(strategy.verify(concept, rowCountVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        assertThat(strategy.verify(constraint, rowCountVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        when(result.size()).thenReturn(1);
        assertThat(strategy.verify(concept, rowCountVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        assertThat(strategy.verify(constraint, rowCountVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        when(result.size()).thenReturn(2);
        assertThat(strategy.verify(concept, rowCountVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        assertThat(strategy.verify(constraint, rowCountVerification, COLUMN_NAMES, result), equalTo(FAILURE));
    }

}
