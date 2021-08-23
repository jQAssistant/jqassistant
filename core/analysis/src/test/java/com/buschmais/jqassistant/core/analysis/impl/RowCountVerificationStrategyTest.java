package com.buschmais.jqassistant.core.analysis.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.reader.RowCountVerification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.FAILURE;
import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RowCountVerificationStrategyTest {

    static final List<String> COLUMN_NAMES = Collections.singletonList("a");

    @Mock
    private Concept concept;

    @Mock
    private Constraint constraint;

    @Mock
    private List<Map<String, Object>> result;

    private RowCountVerificationStrategy strategy = new RowCountVerificationStrategy();

    @Test
    void defaultConfiguration() throws RuleException {
        RowCountVerification rowCountVerification = RowCountVerification.builder().build();
        when(result.size()).thenReturn(0);
        assertThat(strategy.verify(concept, rowCountVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        assertThat(strategy.verify(constraint, rowCountVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        when(result.size()).thenReturn(1);
        assertThat(strategy.verify(concept, rowCountVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        assertThat(strategy.verify(constraint, rowCountVerification, COLUMN_NAMES, result), equalTo(FAILURE));
    }

    @Test
    void min() throws RuleException {
        RowCountVerification rowCountVerification = RowCountVerification.builder().min(1).build();
        when(result.size()).thenReturn(0);
        assertThat(strategy.verify(concept, rowCountVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        assertThat(strategy.verify(constraint, rowCountVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        when(result.size()).thenReturn(1);
        assertThat(strategy.verify(concept, rowCountVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        assertThat(strategy.verify(constraint, rowCountVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
    }

    @Test
    void max() throws RuleException {
        RowCountVerification rowCountVerification = RowCountVerification.builder().max(0).build();
        when(result.size()).thenReturn(0);
        assertThat(strategy.verify(concept, rowCountVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        assertThat(strategy.verify(constraint, rowCountVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        when(result.size()).thenReturn(1);
        assertThat(strategy.verify(concept, rowCountVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        assertThat(strategy.verify(constraint, rowCountVerification, COLUMN_NAMES, result), equalTo(FAILURE));
    }

    @Test
    void minMax() throws RuleException {
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
