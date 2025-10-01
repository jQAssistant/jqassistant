package com.buschmais.jqassistant.core.analysis.impl;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.analysis.api.baseline.BaselineManager;
import com.buschmais.jqassistant.core.analysis.api.configuration.Analyze;
import com.buschmais.jqassistant.core.report.api.configuration.Report;
import com.buschmais.jqassistant.core.report.api.model.*;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.core.shared.map.MapBuilder;
import com.buschmais.jqassistant.core.store.api.Store;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.buschmais.jqassistant.core.rule.api.model.Severity.BLOCKER;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class AnalyzerContextImplTest {

    private static final String CONSTRAINT_ID = "constraint";
    public static final String PRIMARY_COLUMN = "primary";
    public static final String SECONDARY_COLUMN = "secondary";
    public static final String VALID_DATE = "2065-06-01";
    public static final String INVALID_DATE = "2025-01-01";

    private AnalyzerContext analyzerContext;

    @Mock
    private Analyze configuration;

    @Mock
    private Report report;

    @Mock
    private Store store;

    @Mock
    private BaselineManager baselineManager;

    @BeforeEach
    void setUp() throws RuleException {
        doReturn(report).when(configuration)
            .report();
        doReturn(Severity.MINOR.name()).when(report)
            .warnOnSeverity();
        doReturn(Severity.MAJOR.name()).when(report)
            .failOnSeverity();
        analyzerContext = new AnalyzerContextImpl(configuration, this.getClass()
            .getClassLoader(), store, baselineManager);
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
            .map(Row::getKey)
            .collect(toSet());
        assertThat(rowKeys).hasSize(3);
    }

    @Test
    void withoutSuppression() {
        Constraint constraint = getConstraint();
        Row row = analyzerContext.toRow(constraint,
            Map.of(PRIMARY_COLUMN, analyzerContext.toColumn("value1_1"), SECONDARY_COLUMN, analyzerContext.toColumn("value1_2")));

        assertThat(analyzerContext.isSuppressed(constraint, PRIMARY_COLUMN, row)).isFalse();
    }

    @Test
    void suppressByPrimaryColumn() {
        Suppress suppressedValue = createSuppressedValue(empty(), empty(), empty(),CONSTRAINT_ID);
        Constraint constraint = getConstraint();

        Row row = analyzerContext.toRow(constraint,
            Map.of(PRIMARY_COLUMN, analyzerContext.toColumn(suppressedValue), SECONDARY_COLUMN, analyzerContext.toColumn("value")));

        assertThat(analyzerContext.isSuppressed(constraint, PRIMARY_COLUMN, row)).isTrue();
    }

    @Test
    void suppressByNonPrimaryColumn() {
        Suppress suppressedValue = createSuppressedValue(of(SECONDARY_COLUMN), empty(), empty(), CONSTRAINT_ID);
        Constraint constraint = getConstraint();

        Row row = analyzerContext.toRow(constraint,
            Map.of(PRIMARY_COLUMN, analyzerContext.toColumn("value"), SECONDARY_COLUMN, analyzerContext.toColumn(suppressedValue)));

        assertThat(analyzerContext.isSuppressed(constraint, PRIMARY_COLUMN, row)).isTrue();
    }

    @Test
    void nonMatchingSuppressId() {
        Suppress suppressedValue = createSuppressedValue(empty(), empty(),empty(),"otherConstraint");
        Constraint constraint = getConstraint();
        Row row = analyzerContext.toRow(constraint,
            Map.of(PRIMARY_COLUMN, analyzerContext.toColumn(suppressedValue), SECONDARY_COLUMN, analyzerContext.toColumn("value")));

        assertThat(analyzerContext.isSuppressed(constraint, PRIMARY_COLUMN, row)).isFalse();
    }

    @Test
    void validSuppressUntilWithReason() {
        Suppress suppressedValue = createSuppressedValue(empty(), of(VALID_DATE), empty(), CONSTRAINT_ID);
        Constraint constraint = getConstraint();
        Row row = analyzerContext.toRow(constraint,
            Map.of(PRIMARY_COLUMN, analyzerContext.toColumn(suppressedValue), SECONDARY_COLUMN, analyzerContext.toColumn("value")));

        assertThat(analyzerContext.isSuppressed(constraint, PRIMARY_COLUMN, row)).isTrue();
    }

    @Test
    void expiredSuppressUntil() {
        Suppress suppressedValue = createSuppressedValue(empty(), of(INVALID_DATE), empty(),CONSTRAINT_ID);
        Constraint constraint = getConstraint();
        Row row = analyzerContext.toRow(constraint,
            Map.of(PRIMARY_COLUMN, analyzerContext.toColumn(suppressedValue), SECONDARY_COLUMN, analyzerContext.toColumn("value")));

        assertThat(analyzerContext.isSuppressed(constraint, PRIMARY_COLUMN, row)).isFalse();
    }

    @Test
    void getStatus() {
        assertThat(analyzerContext.getStatus(VerificationResult.builder()
            .success(true)
            .build(), BLOCKER)).isEqualTo(Result.Status.SUCCESS);
        assertThat(analyzerContext.getStatus(VerificationResult.builder()
            .success(false)
            .build(), Severity.INFO)).isEqualTo(Result.Status.SUCCESS);
        assertThat(analyzerContext.getStatus(VerificationResult.builder()
            .success(false)
            .build(), Severity.MINOR)).isEqualTo(Result.Status.WARNING);
        assertThat(analyzerContext.getStatus(VerificationResult.builder()
            .success(false)
            .build(), Severity.MAJOR)).isEqualTo(Result.Status.FAILURE);
    }

    private Constraint getConstraint() {
        com.buschmais.jqassistant.core.rule.api.model.Report report = com.buschmais.jqassistant.core.rule.api.model.Report.builder()
            .primaryColumn(PRIMARY_COLUMN)
            .build();
        return Constraint.builder()
            .id(CONSTRAINT_ID)
            .report(report)
            .build();
    }

    private static Suppress createSuppressedValue(Optional<String> suppressColumn, Optional<String> suppressUntil, Optional<String> suppressReason, String... suppressIds) {
        return new Suppress() {
            @Override
            public String[] getSuppressIds() {
                return suppressIds;
            }

            @Override
            public void setSuppressIds(String[] suppressIds1) {
            }

            @Override
            public String getSuppressColumn() {
                return suppressColumn.orElse(null);
            }

            @Override
            public void setSuppressColumn(String suppressColumn1) {
            }

            @Override
            public String getSuppressUntil() {
                return suppressUntil.orElse(null);
            }

            @Override
            public void setSuppressUntil(String suppressUntil) {
            }

            @Override
            public String getSuppressReason() {
                return suppressReason.orElse(null);
            }

            @Override
            public void setSuppressReason(String suppressReason) {
            }
        };
    }
}
