package com.buschmais.jqassistant.core.analysis.impl;

import java.util.List;

import com.buschmais.jqassistant.core.report.api.configuration.Report;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.core.rule.api.reader.RowCountVerification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.*;
import static com.buschmais.jqassistant.core.rule.api.model.Severity.MAJOR;
import static com.buschmais.jqassistant.core.rule.api.model.Severity.MINOR;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RowCountVerificationStrategyTest {

    static final List<String> COLUMN_NAMES = singletonList("a");

    @Mock
    private Report configuration;

    @Mock
    private Concept concept;

    @Mock
    private Constraint constraint;

    @Mock
    private List<Row> result;

    private RowCountVerificationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new RowCountVerificationStrategy(configuration);
        doReturn(Severity.Threshold.from(MAJOR)).when(configuration)
            .failOnSeverity();
        doReturn(Severity.Threshold.from(MINOR)).when(configuration)
            .warnOnSeverity();
    }

    @Test
    void defaultConfiguration() {
        RowCountVerification rowCountVerification = RowCountVerification.builder()
            .build();
        when(result.size()).thenReturn(0);
        assertThat(strategy.verify(concept, MINOR, rowCountVerification, COLUMN_NAMES, result), equalTo(WARNING));
        assertThat(strategy.verify(constraint, MAJOR, rowCountVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        when(result.size()).thenReturn(1);
        assertThat(strategy.verify(concept, MINOR, rowCountVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        assertThat(strategy.verify(constraint, MAJOR, rowCountVerification, COLUMN_NAMES, result), equalTo(FAILURE));
    }

    @Test
    void min() {
        RowCountVerification rowCountVerification = RowCountVerification.builder()
            .min(1)
            .build();
        when(result.size()).thenReturn(0);
        assertThat(strategy.verify(concept, MINOR, rowCountVerification, COLUMN_NAMES, result), equalTo(WARNING));
        assertThat(strategy.verify(constraint, MAJOR, rowCountVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        when(result.size()).thenReturn(1);
        assertThat(strategy.verify(concept, MINOR, rowCountVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        assertThat(strategy.verify(constraint, MAJOR, rowCountVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
    }

    @Test
    void max() {
        RowCountVerification rowCountVerification = RowCountVerification.builder()
            .max(0)
            .build();
        when(result.size()).thenReturn(0);
        assertThat(strategy.verify(concept, MINOR, rowCountVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        assertThat(strategy.verify(constraint, MAJOR, rowCountVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        when(result.size()).thenReturn(1);
        assertThat(strategy.verify(concept, MINOR, rowCountVerification, COLUMN_NAMES, result), equalTo(WARNING));
        assertThat(strategy.verify(constraint, MAJOR, rowCountVerification, COLUMN_NAMES, result), equalTo(FAILURE));
    }

    @Test
    void minMax() {
        RowCountVerification rowCountVerification = RowCountVerification.builder()
            .min(1)
            .max(1)
            .build();
        when(result.size()).thenReturn(0);
        assertThat(strategy.verify(concept, MINOR, rowCountVerification, COLUMN_NAMES, result), equalTo(WARNING));
        assertThat(strategy.verify(constraint, MAJOR, rowCountVerification, COLUMN_NAMES, result), equalTo(FAILURE));
        when(result.size()).thenReturn(1);
        assertThat(strategy.verify(concept, MINOR, rowCountVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        assertThat(strategy.verify(constraint, MAJOR, rowCountVerification, COLUMN_NAMES, result), equalTo(SUCCESS));
        when(result.size()).thenReturn(2);
        assertThat(strategy.verify(concept, MINOR, rowCountVerification, COLUMN_NAMES, result), equalTo(WARNING));
        assertThat(strategy.verify(constraint, MAJOR, rowCountVerification, COLUMN_NAMES, result), equalTo(FAILURE));
    }

}
