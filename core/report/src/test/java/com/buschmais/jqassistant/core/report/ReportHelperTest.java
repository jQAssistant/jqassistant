package com.buschmais.jqassistant.core.report;

import java.util.*;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;
import com.buschmais.jqassistant.core.report.api.ReportHelper;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter;
import com.buschmais.jqassistant.core.report.model.TestDescriptorWithLanguageElement;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Verifies functionality of the report helper.
 */
@RunWith(MockitoJUnitRunner.class)
public class ReportHelperTest {

    @Mock
    private Logger logger;

    @Mock
    private InMemoryReportWriter inMemoryReportWriter;

    private List<String> debugMessages;

    private List<String> warnMessages;

    private List<String> errorMessages;

    private ReportHelper reportHelper;

    @Before
    public void before() {
        reportHelper = new ReportHelper(logger);
        debugMessages = new ArrayList<>();
        warnMessages = new ArrayList<>();
        errorMessages = new ArrayList<>();
        doAnswer(new LogAnswer(debugMessages)).when(logger).debug(Mockito.anyString());
        doAnswer(new LogAnswer(warnMessages)).when(logger).warn(Mockito.anyString());
        doAnswer(new LogAnswer(errorMessages)).when(logger).error(Mockito.anyString());
    }

    @Test
    public void successfulConcept() {
        Result<Concept> conceptResult = mockResult("test:concept", Concept.class, Result.Status.SUCCESS, Severity.MAJOR);
        Map<String, Result<Concept>> conceptResults = new HashMap<>();
        conceptResults.put("test:concept", conceptResult);
        when(inMemoryReportWriter.getConceptResults()).thenReturn(conceptResults);
        int violations = reportHelper.verifyConceptResults(Severity.MINOR, Severity.MAJOR, inMemoryReportWriter);
        assertThat(violations, equalTo(0));
        assertThat(warnMessages.size(), equalTo(0));
        assertThat(debugMessages.size(), equalTo(0));
        assertThat(errorMessages.size(), equalTo(0));
    }

    @Test
    public void failedConcepts() {
        Result<Concept> infoConceptResult = mockResult("test:infoConcept", Concept.class, Result.Status.FAILURE, Severity.INFO);
        Result<Concept> minorConceptResult = mockResult("test:minorConcept", Concept.class, Result.Status.FAILURE, Severity.MINOR);
        Result<Concept> majorConceptResult = mockResult("test:majorConcept", Concept.class, Result.Status.FAILURE, Severity.MAJOR);
        Map<String, Result<Concept>> conceptResults = new HashMap<>();
        conceptResults.put("test:infoConcept", infoConceptResult);
        conceptResults.put("test:minorConcept", minorConceptResult);
        conceptResults.put("test:majorConcept", majorConceptResult);
        when(inMemoryReportWriter.getConceptResults()).thenReturn(conceptResults);
        int violations = reportHelper.verifyConceptResults(Severity.MINOR, Severity.MAJOR, inMemoryReportWriter);
        assertThat(violations, equalTo(1));
        verifyMessages(debugMessages, ReportHelper.CONCEPT_FAILED_HEADER, "Concept: test:infoConcept", "Severity: INFO");
        verifyMessages(warnMessages, ReportHelper.CONCEPT_FAILED_HEADER, "Concept: test:minorConcept", "Severity: MINOR");
        verifyMessages(errorMessages, ReportHelper.CONCEPT_FAILED_HEADER, "Concept: test:majorConcept", "Severity: MAJOR");
    }

    @Test
    public void failedConceptsWithOverriddenSeverity() {
        Result<Concept> infoConceptResult = mockResult("test:infoConcept", Concept.class, Result.Status.FAILURE, Severity.INFO, Severity.MINOR);
        Result<Concept> minorConceptResult = mockResult("test:minorConcept", Concept.class, Result.Status.FAILURE, Severity.MINOR, Severity.MAJOR);
        Result<Concept> majorConceptResult = mockResult("test:majorConcept", Concept.class, Result.Status.FAILURE, Severity.MAJOR, Severity.CRITICAL);
        Map<String, Result<Concept>> conceptResults = new HashMap<>();
        conceptResults.put("test:infoConcept", infoConceptResult);
        conceptResults.put("test:minorConcept", minorConceptResult);
        conceptResults.put("test:majorConcept", majorConceptResult);
        when(inMemoryReportWriter.getConceptResults()).thenReturn(conceptResults);
        int violations = reportHelper.verifyConceptResults(Severity.MAJOR, Severity.CRITICAL, inMemoryReportWriter);
        assertThat(violations, equalTo(1));
        verifyMessages(debugMessages, ReportHelper.CONCEPT_FAILED_HEADER, "Concept: test:infoConcept", "Severity: MINOR (from INFO)");
        verifyMessages(warnMessages, ReportHelper.CONCEPT_FAILED_HEADER, "Concept: test:minorConcept", "Severity: MAJOR (from MINOR)");
        verifyMessages(errorMessages, ReportHelper.CONCEPT_FAILED_HEADER, "Concept: test:majorConcept", "Severity: CRITICAL (from MAJOR)");
    }

    @Test
    public void validatedConstraint() {
        Result<Constraint> constraintResult = mockResult("test:concept", Constraint.class, Result.Status.SUCCESS, Severity.MAJOR);
        Map<String, Result<Constraint>> constraintResults = new HashMap<>();
        constraintResults.put("test:concept", constraintResult);
        when(inMemoryReportWriter.getConstraintResults()).thenReturn(constraintResults);
        int violations = reportHelper.verifyConstraintResults(Severity.MINOR, Severity.MINOR, inMemoryReportWriter);
        assertThat(violations, equalTo(0));
        assertThat(warnMessages.size(), equalTo(0));
        assertThat(debugMessages.size(), equalTo(0));
        assertThat(errorMessages.size(), equalTo(0));
    }

    @Test
    public void failedConstraints() {
        Map<String, Object> infoRow = new HashMap<>();
        infoRow.put("InfoElement", "InfoValue");
        Result<Constraint> infoConstraintResult = mockResult("test:infoConstraint", Constraint.class, Result.Status.FAILURE, Severity.INFO,
                Collections.singletonList(infoRow));
        Map<String, Object> minorRow = new HashMap<>();
        minorRow.put("MinorElement", "MinorValue");
        Result<Constraint> minorConstraintResult = mockResult("test:minorConstraint", Constraint.class, Result.Status.FAILURE, Severity.MINOR,
                Collections.singletonList(minorRow));
        Map<String, Object> majorRow = new HashMap<>();
        majorRow.put("MajorElement", "MajorValue");
        Result<Constraint> majorConstraintResult = mockResult("test:majorConstraint", Constraint.class, Result.Status.FAILURE, Severity.MAJOR,
                Collections.singletonList(majorRow));
        Map<String, Result<Constraint>> constraintResults = new HashMap<>();
        constraintResults.put("test:infoConstraint", infoConstraintResult);
        constraintResults.put("test:minorConstraint", minorConstraintResult);
        constraintResults.put("test:majorConstraint", majorConstraintResult);
        when(inMemoryReportWriter.getConstraintResults()).thenReturn(constraintResults);
        int violations = reportHelper.verifyConstraintResults(Severity.MINOR, Severity.MAJOR, inMemoryReportWriter);
        assertThat(violations, equalTo(1));
        verifyMessages(debugMessages, ReportHelper.CONSTRAINT_VIOLATION_HEADER, "Constraint: test:infoConstraint", "Severity: INFO", "InfoElement=InfoValue");
        verifyMessages(warnMessages, ReportHelper.CONSTRAINT_VIOLATION_HEADER, "Constraint: test:minorConstraint", "Severity: MINOR",
                "MinorElement=MinorValue");
        verifyMessages(errorMessages, ReportHelper.CONSTRAINT_VIOLATION_HEADER, "Constraint: test:majorConstraint", "Severity: MAJOR",
                "MajorElement=MajorValue");
    }

    @Test
    public void failedConstraintsWithOverriddenSeverity() {
        Map<String, Object> infoRow = new HashMap<>();
        infoRow.put("InfoElement", "InfoValue");
        Result<Constraint> infoConstraintResult = mockResult("test:infoConstraint", Constraint.class, Result.Status.FAILURE, Severity.INFO, Severity.MINOR,
                Collections.singletonList(infoRow));
        Map<String, Object> minorRow = new HashMap<>();
        minorRow.put("MinorElement", "MinorValue");
        Result<Constraint> minorConstraintResult = mockResult("test:minorConstraint", Constraint.class, Result.Status.FAILURE, Severity.MINOR, Severity.MAJOR,
                Collections.singletonList(minorRow));
        Map<String, Object> majorRow = new HashMap<>();
        majorRow.put("MajorElement", "MajorValue");
        Result<Constraint> majorConstraintResult = mockResult("test:majorConstraint", Constraint.class, Result.Status.FAILURE, Severity.MAJOR,
                Severity.CRITICAL, Collections.singletonList(majorRow));
        Map<String, Result<Constraint>> constraintResults = new HashMap<>();
        constraintResults.put("test:infoConstraint", infoConstraintResult);
        constraintResults.put("test:minorConstraint", minorConstraintResult);
        constraintResults.put("test:majorConstraint", majorConstraintResult);
        when(inMemoryReportWriter.getConstraintResults()).thenReturn(constraintResults);
        int violations = reportHelper.verifyConstraintResults(Severity.MAJOR, Severity.CRITICAL, inMemoryReportWriter);
        assertThat(violations, equalTo(1));
        verifyMessages(debugMessages, ReportHelper.CONSTRAINT_VIOLATION_HEADER, "Constraint: test:infoConstraint", "Severity: MINOR (from INFO)",
                "InfoElement=InfoValue");
        verifyMessages(warnMessages, ReportHelper.CONSTRAINT_VIOLATION_HEADER, "Constraint: test:minorConstraint", "Severity: MAJOR (from MINOR)",
                "MinorElement=MinorValue");
        verifyMessages(errorMessages, ReportHelper.CONSTRAINT_VIOLATION_HEADER, "Constraint: test:majorConstraint", "Severity: CRITICAL (from MAJOR)",
                "MajorElement=MajorValue");
    }

    @Test
    public void label() {
        TestDescriptorWithLanguageElement descriptorWithLabel = mock(TestDescriptorWithLanguageElement.class);
        when(descriptorWithLabel.getValue()).thenReturn("value");
        assertThat(ReportHelper.getLabel(descriptorWithLabel), equalTo("value"));
        assertThat(ReportHelper.getLabel(Collections.singletonList(descriptorWithLabel)), equalTo("[value]"));
        assertThat(ReportHelper.getLabel(new String[] { "value1", "value2" }), equalTo("[value1,value2]"));
        Map<String, Object> map = new HashMap<>();
        map.put("key1", descriptorWithLabel);
        map.put("key2", "simpleValue");
        assertThat(ReportHelper.getLabel(map), equalTo("{key1:value,key2:simpleValue}"));
        TestDescriptorWithLanguageElement descriptorWithEmptyLanguageLabel = mock(TestDescriptorWithLanguageElement.class);
        assertThat(ReportHelper.getLabel(descriptorWithEmptyLanguageLabel), notNullValue());
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
        return mockResult(id, ruleType, status, ruleSeverity, ruleSeverity, Collections.<Map<String, Object>> emptyList());
    }

    private <T extends ExecutableRule> Result<T> mockResult(String id, Class<T> ruleType, Result.Status status, Severity ruleSeverity,
            List<Map<String, Object>> rows) {
        return mockResult(id, ruleType, status, ruleSeverity, ruleSeverity, rows);
    }

    private <T extends ExecutableRule> Result<T> mockResult(String id, Class<T> ruleType, Result.Status status, Severity ruleSeverity,
            Severity effectiveSeverity) {
        return mockResult(id, ruleType, status, ruleSeverity, effectiveSeverity, Collections.<Map<String, Object>> emptyList());
    }

    private <T extends ExecutableRule> Result<T> mockResult(String id, Class<T> ruleType, Result.Status status, Severity ruleSeverity,
            Severity effectiveSeverity, List<Map<String, Object>> rows) {
        Result<T> ruleResult = mock(Result.class);
        T rule = mock(ruleType);
        when(rule.getId()).thenReturn(id);
        when(rule.getDescription()).thenReturn("A\ndescription\r\n.\r");
        when(rule.getSeverity()).thenReturn(ruleSeverity);
        when(ruleResult.getRule()).thenReturn(rule);
        when(ruleResult.getStatus()).thenReturn(status);
        when(ruleResult.getSeverity()).thenReturn(effectiveSeverity);
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
