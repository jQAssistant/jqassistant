package com.buschmais.jqassistant.core.report.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.report.api.configuration.Report;
import com.buschmais.jqassistant.core.report.api.model.Column;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportPlugin;
import com.buschmais.jqassistant.core.report.model.TestDescriptorWithLanguageElement;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.xo.api.CompositeObject;
import com.buschmais.xo.neo4j.api.model.Neo4jNode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;

import static com.buschmais.jqassistant.core.report.api.ReportHelper.toColumn;
import static com.buschmais.jqassistant.core.report.api.model.Result.Status.*;
import static com.buschmais.jqassistant.core.rule.api.model.Severity.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * Verifies functionality of the report helper.
 */
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class ReportHelperTest {

    @Mock
    private Report report;

    @Mock
    private Logger logger;

    @Mock
    private InMemoryReportPlugin inMemoryReportWriter;

    private List<String> debugMessages;

    private List<String> warnMessages;

    private List<String> errorMessages;

    private ReportHelper reportHelper;

    @BeforeEach
    void before() {
        reportHelper = new ReportHelper(report, logger);
        debugMessages = new ArrayList<>();
        warnMessages = new ArrayList<>();
        errorMessages = new ArrayList<>();
        doAnswer(new LogAnswer(debugMessages)).when(logger)
            .debug(Mockito.anyString());
        doAnswer(new LogAnswer(warnMessages)).when(logger)
            .warn(Mockito.anyString());
        doAnswer(new LogAnswer(errorMessages)).when(logger)
            .error(Mockito.anyString());
        doReturn(Severity.MINOR).when(report)
            .warnOnSeverity();
        doReturn(Severity.MAJOR).when(report)
            .failOnSeverity();
    }

    @Test
    void successfulConcept() {
        Result<Concept> conceptResult = mockResult("test:concept", Concept.class, SUCCESS, MAJOR);
        Map<String, Result<Concept>> conceptResults = new HashMap<>();
        conceptResults.put("test:concept", conceptResult);
        when(inMemoryReportWriter.getConceptResults()).thenReturn(conceptResults);

        int violations = reportHelper.verifyConceptResults(inMemoryReportWriter);

        assertThat(violations).isEqualTo(0);
        assertThat(debugMessages.size()).isGreaterThan(0);
        assertThat(warnMessages.size()).isEqualTo(0);
        assertThat(errorMessages.size()).isEqualTo(0);
    }

    @Test
    void failedConcepts() {
        Result<Concept> infoConceptResult = mockResult("test:infoConcept", Concept.class, SUCCESS, INFO);
        Result<Concept> minorConceptResult = mockResult("test:minorConcept", Concept.class, WARNING, MINOR);
        Result<Concept> majorConceptResult = mockResult("test:majorConcept", Concept.class, FAILURE, MAJOR);
        Map<String, Result<Concept>> conceptResults = new HashMap<>();
        conceptResults.put("test:infoConcept", infoConceptResult);
        conceptResults.put("test:minorConcept", minorConceptResult);
        conceptResults.put("test:majorConcept", majorConceptResult);
        when(inMemoryReportWriter.getConceptResults()).thenReturn(conceptResults);

        int violations = reportHelper.verifyConceptResults(inMemoryReportWriter);

        assertThat(violations).isEqualTo(1);
        verifyMessages(debugMessages, ReportHelper.CONCEPT_FAILED_HEADER, "Concept: test:infoConcept", "Severity: INFO");
        verifyMessages(warnMessages, ReportHelper.CONCEPT_FAILED_HEADER, "Concept: test:minorConcept", "Severity: MINOR");
        verifyMessages(errorMessages, ReportHelper.CONCEPT_FAILED_HEADER, "Concept: test:majorConcept", "Severity: MAJOR");
    }

    @Test
    void failedConceptsWithOverriddenSeverity() {
        doReturn(Severity.MAJOR).when(report)
            .warnOnSeverity();
        doReturn(Severity.CRITICAL).when(report)
            .failOnSeverity();
        Result<Concept> infoConceptResult = mockResult("test:infoConcept", Concept.class, SUCCESS, INFO, MINOR);
        Result<Concept> minorConceptResult = mockResult("test:minorConcept", Concept.class, WARNING, MINOR, MAJOR);
        Result<Concept> majorConceptResult = mockResult("test:majorConcept", Concept.class, FAILURE, MAJOR, CRITICAL);
        Map<String, Result<Concept>> conceptResults = new HashMap<>();
        conceptResults.put("test:infoConcept", infoConceptResult);
        conceptResults.put("test:minorConcept", minorConceptResult);
        conceptResults.put("test:majorConcept", majorConceptResult);
        when(inMemoryReportWriter.getConceptResults()).thenReturn(conceptResults);

        int violations = reportHelper.verifyConceptResults(inMemoryReportWriter);

        assertThat(violations).isEqualTo(1);
        verifyMessages(debugMessages, ReportHelper.CONCEPT_FAILED_HEADER, "Concept: test:infoConcept", "Severity: MINOR (from INFO)");
        verifyMessages(warnMessages, ReportHelper.CONCEPT_FAILED_HEADER, "Concept: test:minorConcept", "Severity: MAJOR (from MINOR)");
        verifyMessages(errorMessages, ReportHelper.CONCEPT_FAILED_HEADER, "Concept: test:majorConcept", "Severity: CRITICAL (from MAJOR)");
    }

    @Test
    void validatedConstraint() {
        doReturn(Severity.MINOR).when(report)
            .failOnSeverity();
        Result<Constraint> constraintResult = mockResult("test:constraint", Constraint.class, SUCCESS, MAJOR);
        Map<String, Result<Constraint>> constraintResults = new HashMap<>();
        constraintResults.put("test:constraint", constraintResult);
        when(inMemoryReportWriter.getConstraintResults()).thenReturn(constraintResults);

        int violations = reportHelper.verifyConstraintResults(inMemoryReportWriter);

        assertThat(violations).isEqualTo(0);
        assertThat(debugMessages.size()).isGreaterThan(0);
        assertThat(warnMessages.size()).isEqualTo(0);
        assertThat(errorMessages.size()).isEqualTo(0);
    }

    @Test
    void failedConstraints() {
        Map<String, Column<?>> infoRow = new HashMap<>();
        infoRow.put("InfoElement", toColumn("InfoValue"));
        Result<Constraint> infoConstraintResult = mockResult("test:infoConstraint", Constraint.class, SUCCESS, INFO, singletonList(
            Row.builder()
                .key("1")
                .columns(infoRow)
                .build()));
        Map<String, Column<?>> minorRow = new HashMap<>();
        minorRow.put("MinorElement", toColumn("MinorValue"));
        Result<Constraint> minorConstraintResult = mockResult("test:minorConstraint", Constraint.class, WARNING, MINOR, singletonList(
            Row.builder()
                .key("2")
                .columns(minorRow)
                .build()));
        Map<String, Column<?>> majorRow = new HashMap<>();
        majorRow.put("MajorElement", toColumn("MajorValue"));
        Result<Constraint> majorConstraintResult = mockResult("test:majorConstraint", Constraint.class, FAILURE, MAJOR, singletonList(
            Row.builder()
                .key("3")
                .columns(majorRow)
                .build()));
        Map<String, Result<Constraint>> constraintResults = new HashMap<>();
        constraintResults.put("test:infoConstraint", infoConstraintResult);
        constraintResults.put("test:minorConstraint", minorConstraintResult);
        constraintResults.put("test:majorConstraint", majorConstraintResult);
        when(inMemoryReportWriter.getConstraintResults()).thenReturn(constraintResults);

        int violations = reportHelper.verifyConstraintResults(inMemoryReportWriter);

        assertThat(violations).isEqualTo(1);
        verifyMessages(debugMessages, ReportHelper.CONSTRAINT_VIOLATION_HEADER, "Constraint: test:infoConstraint", "Severity: INFO", "InfoElement=InfoValue");
        verifyMessages(warnMessages, ReportHelper.CONSTRAINT_VIOLATION_HEADER, "Constraint: test:minorConstraint", "Severity: MINOR",
            "MinorElement=MinorValue");
        verifyMessages(errorMessages, ReportHelper.CONSTRAINT_VIOLATION_HEADER, "Constraint: test:majorConstraint", "Severity: MAJOR",
            "MajorElement=MajorValue");
    }

    @Test
    void failedConstraintsWithOverriddenSeverity() {
        Map<String, Column<?>> infoRow = new HashMap<>();
        infoRow.put("InfoElement", toColumn("InfoValue"));
        Result<Constraint> infoConstraintResult = mockResult("test:infoConstraint", Constraint.class, SUCCESS, INFO, MINOR,
            singletonList(Row.builder()
                .key("1")
                .columns(infoRow)
                .build()));
        Map<String, Column<?>> minorRow = new HashMap<>();
        minorRow.put("MinorElement", toColumn("MinorValue"));
        Result<Constraint> minorConstraintResult = mockResult("test:minorConstraint", Constraint.class, WARNING, MINOR, MAJOR,
            singletonList(Row.builder()
                .key("2")
                .columns(minorRow)
                .build()));
        Map<String, Column<?>> majorRow = new HashMap<>();
        majorRow.put("MajorElement", toColumn("MajorValue"));
        Result<Constraint> majorConstraintResult = mockResult("test:majorConstraint", Constraint.class, FAILURE, MAJOR, CRITICAL, singletonList(Row.builder()
                .key("3")
                .columns(majorRow)
                .build()));
        Map<String, Result<Constraint>> constraintResults = new HashMap<>();
        constraintResults.put("test:infoConstraint", infoConstraintResult);
        constraintResults.put("test:minorConstraint", minorConstraintResult);
        constraintResults.put("test:majorConstraint", majorConstraintResult);
        when(inMemoryReportWriter.getConstraintResults()).thenReturn(constraintResults);

        int violations = reportHelper.verifyConstraintResults(inMemoryReportWriter);

        assertThat(violations).isEqualTo(1);
        verifyMessages(debugMessages, ReportHelper.CONSTRAINT_VIOLATION_HEADER, "Constraint: test:infoConstraint", "Severity: MINOR (from INFO)",
            "InfoElement=InfoValue");
        verifyMessages(warnMessages, ReportHelper.CONSTRAINT_VIOLATION_HEADER, "Constraint: test:minorConstraint", "Severity: MAJOR (from MINOR)",
            "MinorElement=MinorValue");
        verifyMessages(errorMessages, ReportHelper.CONSTRAINT_VIOLATION_HEADER, "Constraint: test:majorConstraint", "Severity: CRITICAL (from MAJOR)",
            "MajorElement=MajorValue");
    }

    @Test
    void continueOnFailureEnabled() throws ReportException {
        doReturn(Severity.MAJOR).when(report)
            .failOnSeverity();
        doReturn(true).when(report)
            .continueOnFailure();

        Result<Concept> conceptResult = mockResult("test:concept", Concept.class, FAILURE, MAJOR);
        Map<String, Result<Concept>> conceptResults = new HashMap<>();
        conceptResults.put("test:concept", conceptResult);
        when(inMemoryReportWriter.getConceptResults()).thenReturn(conceptResults);

        Result<Constraint> constraintResult = mockResult("test:constraint", Constraint.class, FAILURE, MAJOR);
        Map<String, Result<Constraint>> constraintResults = new HashMap<>();
        constraintResults.put("test:constraint", constraintResult);
        when(inMemoryReportWriter.getConstraintResults()).thenReturn(constraintResults);

        boolean result = reportHelper.verify(inMemoryReportWriter, message -> {
            throw new ReportException(message);
        });

        assertThat(result).isEqualTo(false);
    }

    @Test
    void continueOnFailureDisabled() {
        doReturn(Severity.MINOR).when(report)
            .warnOnSeverity();
        doReturn(Severity.MAJOR).when(report)
            .failOnSeverity();
        doReturn(false).when(report)
            .continueOnFailure();

        Result<Concept> conceptResult1 = mockResult("test:concept1", Concept.class, SUCCESS, MAJOR);
        Result<Concept> conceptResult2 = mockResult("test:concept2", Concept.class, WARNING, MINOR);
        Result<Concept> conceptResult3 = mockResult("test:concept3", Concept.class, FAILURE, MAJOR);
        Map<String, Result<Concept>> conceptResults = new HashMap<>();
        conceptResults.put("test:concept1", conceptResult1);
        conceptResults.put("test:concept2", conceptResult2);
        conceptResults.put("test:concept3", conceptResult3);
        when(inMemoryReportWriter.getConceptResults()).thenReturn(conceptResults);

        Result<Constraint> constraintResult1 = mockResult("test:constraint1", Constraint.class, SUCCESS, MAJOR);
        Result<Constraint> constraintResult2 = mockResult("test:constraint2", Constraint.class, WARNING, MINOR);
        Result<Constraint> constraintResult3 = mockResult("test:constraint3", Constraint.class, FAILURE, MAJOR);
        Map<String, Result<Constraint>> constraintResults = new HashMap<>();
        constraintResults.put("test:constraint1", constraintResult1);
        constraintResults.put("test:constraint2", constraintResult2);
        constraintResults.put("test:constraint3", constraintResult3);
        when(inMemoryReportWriter.getConstraintResults()).thenReturn(constraintResults);

        try {
            reportHelper.verify(inMemoryReportWriter, message -> {
                throw new ReportException(message);
            });
            fail("Expecting a " + ReportException.class);
        } catch (ReportException e) {
            assertThat(e.getMessage()).isEqualTo("Failed rules detected: " + 1 + " concepts, " + 1 + " constraints");
        }
    }

    @Test
    void label() {
        TestDescriptorWithLanguageElement descriptorWithLabel = mock(TestDescriptorWithLanguageElement.class);
        when(descriptorWithLabel.getValue()).thenReturn("value");
        assertThat(ReportHelper.getLabel(descriptorWithLabel)).isEqualTo("value");
        assertThat(ReportHelper.getLabel(singletonList(descriptorWithLabel))).isEqualTo("value");
        assertThat(ReportHelper.getLabel(new String[]{"value1", "value2"})).isEqualTo("value1, value2");
        Map<String, Object> singleProperty = new HashMap<>();
        singleProperty.put("key", "Value");
        assertThat(ReportHelper.getLabel(singleProperty)).isEqualTo("Value");
        Map<String, Object> multipleProperties = new HashMap<>();
        multipleProperties.put("key1", descriptorWithLabel);
        multipleProperties.put("key2", "simpleValue");
        assertThat(ReportHelper.getLabel(multipleProperties)).isEqualTo("key1:value, key2:simpleValue");
        TestDescriptorWithLanguageElement descriptorWithEmptyLanguageLabel = mock(TestDescriptorWithLanguageElement.class);
        assertThat(ReportHelper.getLabel(descriptorWithEmptyLanguageLabel)).isNotNull();

        // Composite object without supported label
        Neo4jNode neo4jNode = mock(Neo4jNode.class);
        doReturn(multipleProperties).when(neo4jNode)
            .getProperties();
        CompositeObject compositeObject = mock(CompositeObject.class);
        doReturn(neo4jNode).when(compositeObject)
            .getDelegate();
        assertThat(ReportHelper.getLabel(compositeObject)).isEqualTo("key1:value, key2:simpleValue");

    }

    @Test
    void ruleWithoutDescription() {
        Result<Constraint> constraintResult = mockResult("test:constraint", null, Constraint.class, FAILURE, MAJOR, MAJOR, emptyList());
        Map<String, Result<Constraint>> constraintResults = new HashMap<>();
        constraintResults.put("test:constraint", constraintResult);
        when(inMemoryReportWriter.getConstraintResults()).thenReturn(constraintResults);

        try {
            reportHelper.verify(inMemoryReportWriter, message -> {
                throw new ReportException(message);
            });
            fail("Expecting a " + ReportException.class);
        } catch (ReportException e) {
            assertThat(e.getMessage()).isEqualTo("Failed rules detected: " + 0 + " concepts, " + 1 + " constraints");
        }
    }

    private void verifyMessages(List<String> messages, String expectedHeader, String expectedRule, String expectedSeverity, String expectedRow) {
        verifyMessages(messages, expectedHeader, expectedRule, expectedSeverity);
        assertThat(messages, hasItem(containsString(expectedRow)));
    }

    private void verifyMessages(List<String> messages, String expectedHeader, String expectedRule, String expectedSeverity) {
        assertThat(messages, hasItem(expectedHeader));
        assertThat(messages, hasItem(expectedRule));
        assertThat(messages, hasItem(expectedSeverity));
    }

    private <T extends ExecutableRule> Result<T> mockResult(String id, Class<T> ruleType, Result.Status status, Severity ruleSeverity) {
        return mockResult(id, ruleType, status, ruleSeverity, ruleSeverity, emptyList());
    }

    private <T extends ExecutableRule> Result<T> mockResult(String id, Class<T> ruleType, Result.Status status, Severity ruleSeverity, List<Row> rows) {
        return mockResult(id, ruleType, status, ruleSeverity, ruleSeverity, rows);
    }

    private <T extends ExecutableRule> Result<T> mockResult(String id, Class<T> ruleType, Result.Status status, Severity ruleSeverity,
        Severity effectiveSeverity) {
        return mockResult(id, ruleType, status, ruleSeverity, effectiveSeverity, emptyList());
    }

    private <T extends ExecutableRule> Result<T> mockResult(String id, Class<T> ruleType, Result.Status status, Severity ruleSeverity, Severity resultSeverity,
        List<Row> rows) {
        return mockResult(id, "A\ndescription\r\n.\r", ruleType, status, ruleSeverity, resultSeverity, rows);
    }

    private <T extends ExecutableRule> Result<T> mockResult(String id, String description, Class<T> ruleType, Result.Status status, Severity ruleSeverity,
        Severity resultSeverity,
        List<Row> rows) {
        Result<T> ruleResult = mock(Result.class);
        T rule = mock(ruleType);
        when(rule.getId()).thenReturn(id);
        when(rule.getDescription()).thenReturn(description);
        when(rule.getSeverity()).thenReturn(ruleSeverity);
        when(ruleResult.getRule()).thenReturn(rule);
        when(ruleResult.getStatus()).thenReturn(status);
        when(ruleResult.getSeverity()).thenReturn(resultSeverity);
        when(ruleResult.getRows()).thenReturn(rows);
        return ruleResult;
    }

    private class LogAnswer implements Answer {

        private List<String> messages;

        private LogAnswer(List<String> messages) {
            this.messages = messages;
        }

        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            String message = (String) invocation.getArguments()[0];
            messages.add(message);
            return null;
        }
    }
}
