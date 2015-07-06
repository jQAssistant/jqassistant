package com.buschmais.jqassistant.core.report;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.buschmais.jqassistant.core.analysis.api.Console;
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
    private Console console;

    @Mock
    private InMemoryReportWriter inMemoryReportWriter;

    private ReportHelper reportHelper;

    @Before
    public void before() {
        reportHelper = new ReportHelper(console);
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
    }

    private <T extends ExecutableRule> Result<T> mockResult(String id, Class<T> ruleType, Result.Status status, Severity severity) {
        Result<T> ruleResult = mock(Result.class);
        T rule = mock(ruleType);
        when(rule.getId()).thenReturn(id);
        when(ruleResult.getRule()).thenReturn(rule);
        when(ruleResult.getStatus()).thenReturn(status);
        when(ruleResult.getSeverity()).thenReturn(severity);
        return ruleResult;
    }
}
