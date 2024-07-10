package com.buschmais.jqassistant.core.analysis.impl;

import java.util.Set;
import java.util.stream.Stream;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.analysis.api.configuration.Analyze;
import com.buschmais.jqassistant.core.report.api.configuration.Report;
import com.buschmais.jqassistant.core.report.api.model.Column;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.core.shared.map.MapBuilder;
import com.buschmais.jqassistant.core.store.api.Store;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class AnalyzerContextImplTest {

    private AnalyzerContext analyzerContext;

    @Mock
    private Analyze configuration;

    @Mock
    private Report report;

    @Mock
    private Store store;

    @BeforeEach
    void setUp() throws RuleException {
        doReturn(report).when(configuration)
            .report();
        doReturn(Severity.MINOR.name()).when(report)
            .warnOnSeverity();
        doReturn(Severity.MAJOR.name()).when(report)
            .failOnSeverity();
        analyzerContext = new AnalyzerContextImpl(configuration, this.getClass()
            .getClassLoader(), store);
    }

    @Test
    void createUniqueRowKeys() {
        Concept concept1 = Concept.builder()
            .id("id1")
            .build();
        Concept concept2 = Concept.builder()
            .id("id2")
            .build();

        Row row1_1 = analyzerContext.toRow(concept1, MapBuilder.<String, Column<?>>builder()
            .entry("c1", analyzerContext.toColumn("v1"))
            .entry("c2", analyzerContext.toColumn("v2"))
            .build());
        Row row1_2 = analyzerContext.toRow(concept1, MapBuilder.<String, Column<?>>builder()
            .entry("c1", analyzerContext.toColumn("v1"))
            .entry("c2", analyzerContext.toColumn("v3"))
            .build());
        Row row2_1 = analyzerContext.toRow(concept2, MapBuilder.<String, Column<?>>builder()
            .entry("c1", analyzerContext.toColumn("v1"))
            .entry("c2", analyzerContext.toColumn("v2"))
            .build());

        Set<String> rowKeys = Stream.of(row1_1, row1_2, row2_1)
            .map(row -> row.getKey())
            .collect(toSet());
        assertThat(rowKeys).hasSize(3);
    }

}
