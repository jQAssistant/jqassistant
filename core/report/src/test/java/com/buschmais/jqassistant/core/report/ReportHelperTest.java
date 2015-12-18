package com.buschmais.jqassistant.core.report;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;
import com.buschmais.jqassistant.core.report.api.ReportHelper;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter;

/**
 * Verifies functionality of the report helper.
 */
@RunWith(MockitoJUnitRunner.class)
public class ReportHelperTest {

    @Mock
    private Logger logger;

    @Mock
    private InMemoryReportWriter inMemoryReportWriter;

    private List<String> warnMessages;

    private List<String> errorMessages;

    private ReportHelper reportHelper;

    @Before
    public void before() {
        reportHelper = new ReportHelper(logger);
        warnMessages = new ArrayList<>();
        errorMessages = new ArrayList<>();
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String message = (String) invocation.getArguments()[0];
                warnMessages.add(message);
                return null;
            }
        }).when(logger).warn(Mockito.anyString());
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String message = (String) invocation.getArguments()[0];
                errorMessages.add(message);
                return null;
            }
        }).when(logger).error(Mockito.anyString());
    }

    @Test
    public void successfulConcept() {
        Result<Concept> conceptResult = mockResult("test:concept", Concept.class, Result.Status.SUCCESS, Severity.MAJOR);
        Map<String, Result<Concept>> conceptResults = new HashMap<>();
        conceptResults.put("test:concept", conceptResult);
        when(inMemoryReportWriter.getConceptResults()).thenReturn(conceptResults);
        int violations = reportHelper.verifyConceptResults(Severity.MINOR, inMemoryReportWriter);
        assertThat(violations, equalTo(0));
    }

    @Test
    public void failedConcepts() {
        Result<Concept> minorConceptResult = mockResult("test:minorConcept", Concept.class, Result.Status.FAILURE, Severity.MINOR);
        Result<Concept> majorConceptResult = mockResult("test:majorConcept", Concept.class, Result.Status.FAILURE, Severity.MAJOR);
        Map<String, Result<Concept>> conceptResults = new HashMap<>();
        conceptResults.put("test:minorConcept", minorConceptResult);
        conceptResults.put("test:majorConcept", majorConceptResult);
        when(inMemoryReportWriter.getConceptResults()).thenReturn(conceptResults);
        int violations = reportHelper.verifyConceptResults(Severity.MAJOR, inMemoryReportWriter);
        assertThat(violations, equalTo(1));
        assertThat(warnMessages, hasItem("Concept failed: test:minorConcept, Severity: MINOR"));
        assertThat(errorMessages, hasItem(ReportHelper.CONCEPT_FAILED_HEADER));
        assertThat(errorMessages, hasItem("Concept: test:majorConcept"));
        assertThat(errorMessages, hasItem("Severity: MAJOR"));
    }

    @Test
    public void failedConceptsWithOverriddenSeverity() {
        Result<Concept> minorConceptResult = mockResult("test:minorConcept", Concept.class, Result.Status.FAILURE, Severity.MINOR);
        Result<Concept> majorConceptResult =
                mockResult("test:majorConcept", Concept.class, Result.Status.FAILURE, Severity.MINOR, Severity.MAJOR);
        Map<String, Result<Concept>> conceptResults = new HashMap<>();
        conceptResults.put("test:minorConcept", minorConceptResult);
        conceptResults.put("test:majorConcept", majorConceptResult);
        when(inMemoryReportWriter.getConceptResults()).thenReturn(conceptResults);
        int violations = reportHelper.verifyConceptResults(Severity.MAJOR, inMemoryReportWriter);
        assertThat(violations, equalTo(1));
        assertThat(warnMessages, hasItem("Concept failed: test:minorConcept, Severity: MINOR"));
        assertThat(errorMessages, hasItem(ReportHelper.CONCEPT_FAILED_HEADER));
        assertThat(errorMessages, hasItem("Concept: test:majorConcept"));
        assertThat(errorMessages, hasItem("Severity: MAJOR (from MINOR)"));
    }

    @Test
    public void validatedConstraint() {
        Result<Constraint> constraintResult = mockResult("test:concept", Constraint.class, Result.Status.SUCCESS, Severity.MAJOR);
        Map<String, Result<Constraint>> constraintResults = new HashMap<>();
        constraintResults.put("test:concept", constraintResult);
        when(inMemoryReportWriter.getConstraintResults()).thenReturn(constraintResults);
        int violations = reportHelper.verifyConstraintResults(Severity.MINOR, inMemoryReportWriter);
        assertThat(violations, equalTo(0));
    }

    @Test
    public void failedConstraints() {
        Result<Constraint> minorConstraintResult = mockResult("test:minorConstraint", Constraint.class, Result.Status.FAILURE, Severity.MINOR);
        Result<Constraint> majorConstraintResult = mockResult("test:majorConstraint", Constraint.class, Result.Status.FAILURE, Severity.MAJOR);
        Map<String, Result<Constraint>> constraintResults = new HashMap<>();
        constraintResults.put("test:minorConstraint", minorConstraintResult);
        constraintResults.put("test:majorConstraint", majorConstraintResult);
        when(inMemoryReportWriter.getConstraintResults()).thenReturn(constraintResults);
        int violations = reportHelper.verifyConstraintResults(Severity.MAJOR, inMemoryReportWriter);
        assertThat(violations, equalTo(1));
        assertThat(warnMessages, hasItem("Constraint failed: test:minorConstraint, Severity: MINOR"));
        assertThat(errorMessages, hasItem(ReportHelper.CONSTRAINT_VIOLATION_HEADER));
        assertThat(errorMessages, hasItem("Constraint: test:majorConstraint"));
        assertThat(errorMessages, hasItem("Severity: MAJOR"));
    }

    @Test
    public void failedConstraintsWithOverriddenSeverity() {
        Result<Constraint> minorConstraintResult = mockResult("test:minorConstraint", Constraint.class, Result.Status.FAILURE, Severity.MINOR);
        Result<Constraint> majorConstraintResult =
                mockResult("test:majorConstraint", Constraint.class, Result.Status.FAILURE, Severity.MINOR, Severity.MAJOR);
        Map<String, Result<Constraint>> constraintResults = new HashMap<>();
        constraintResults.put("test:minorConstraint", minorConstraintResult);
        constraintResults.put("test:majorConstraint", majorConstraintResult);
        when(inMemoryReportWriter.getConstraintResults()).thenReturn(constraintResults);
        int violations = reportHelper.verifyConstraintResults(Severity.MAJOR, inMemoryReportWriter);
        assertThat(violations, equalTo(1));
        assertThat(warnMessages, hasItem("Constraint failed: test:minorConstraint, Severity: MINOR"));
        assertThat(errorMessages, hasItem(ReportHelper.CONSTRAINT_VIOLATION_HEADER));
        assertThat(errorMessages, hasItem("Constraint: test:majorConstraint"));
        assertThat(errorMessages, hasItem("Severity: MAJOR (from MINOR)"));
    }

    private <T extends ExecutableRule> Result<T> mockResult(String id, Class<T> ruleType, Result.Status status, Severity ruleSeverity) {
        return mockResult(id, ruleType, status, ruleSeverity, ruleSeverity);
    }

    private <T extends ExecutableRule> Result<T> mockResult(String id, Class<T> ruleType, Result.Status status, Severity ruleSeverity,
            Severity effectiveSeverity) {
        Result<T> ruleResult = mock(Result.class);
        T rule = mock(ruleType);
        when(rule.getId()).thenReturn(id);
        when(rule.getDescription()).thenReturn("A\ndescription\r\n.\r");
        when(rule.getSeverity()).thenReturn(ruleSeverity);
        when(ruleResult.getRule()).thenReturn(rule);
        when(ruleResult.getStatus()).thenReturn(status);
        when(ruleResult.getSeverity()).thenReturn(effectiveSeverity);
        return ruleResult;
    }
}
