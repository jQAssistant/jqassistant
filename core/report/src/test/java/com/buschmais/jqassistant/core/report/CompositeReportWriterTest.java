package com.buschmais.jqassistant.core.report;


import com.buschmais.jqassistant.core.analysis.api.AnalysisListener;
import com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.report.impl.CompositeReportWriter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CompositeReportWriterTest {

    @Mock
    AnalysisListener reportWriter1;

    @Mock
    AnalysisListener reportWriter2;

    @Mock
    private Group group;

    @Mock
    private Result<?> conceptResult;

    @Mock
    private Result<?> constraintResult;

    private CompositeReportWriter compositeReportWriter;

    @Before
    public void createReportWriter() {
        Map<String, AnalysisListener> reportWriters = new HashMap<>();
        reportWriters.put("writer1", reportWriter1);
        reportWriters.put("writer2", reportWriter2);
        compositeReportWriter = new CompositeReportWriter(reportWriters);
    }

    @Test
    public void noSelection() throws AnalysisListenerException {
        Concept concept = getRule(Concept.class);
        Constraint constraint = getRule(Constraint.class);

        write(concept, constraint);

        verifyInvoked(reportWriter1, concept);
        verifyInvoked(reportWriter1, constraint);
        verifyInvoked(reportWriter2, concept);
        verifyInvoked(reportWriter2, constraint);
        verifyGroup();
    }

    @Test
    public void selectOneWriter() throws AnalysisListenerException {
        Concept concept = getRule(Concept.class, "writer1");
        Constraint constraint = getRule(Constraint.class, "writer1");

        write(concept, constraint);

        verifyInvoked(reportWriter1, concept);
        verifyInvoked(reportWriter1, constraint);
        verifyNotInvoked(reportWriter2, concept);
        verifyNotInvoked(reportWriter2, constraint);
        verifyGroup();
    }

    @Test
    public void selectMultipleWriters() throws AnalysisListenerException {
        Concept concept = getRule(Concept.class, "writer1", "writer2");
        Constraint constraint = getRule(Constraint.class, "writer1", "writer2");

        write(concept, constraint);

        verifyInvoked(reportWriter1, concept);
        verifyInvoked(reportWriter1, constraint);
        verifyInvoked(reportWriter2, concept);
        verifyInvoked(reportWriter2, constraint);
        verifyGroup();
    }

    private void verifyGroup() throws AnalysisListenerException {
        for (AnalysisListener reportWriter : Arrays.asList(reportWriter1, reportWriter2)) {
            verify(reportWriter).begin();
            verify(reportWriter).end();
            verify(reportWriter).beginGroup(group);
            verify(reportWriter).endGroup();
        }
    }

    private void verifyInvoked(AnalysisListener reportWriter, Concept concept) throws AnalysisListenerException {
        verify(reportWriter).beginConcept(concept);
        verify(reportWriter).setResult(conceptResult);
        verify(reportWriter).endConcept();
    }

    private void verifyNotInvoked(AnalysisListener reportWriter, Concept concept) throws AnalysisListenerException {
        verify(reportWriter, never()).beginConcept(concept);
        verify(reportWriter, never()).setResult(conceptResult);
        verify(reportWriter, never()).endConcept();
    }

    private void verifyInvoked(AnalysisListener reportWriter, Constraint constraint) throws AnalysisListenerException {
        verify(reportWriter).beginConstraint(constraint);
        verify(reportWriter).setResult(constraintResult);
        verify(reportWriter).endConstraint();
    }

    private void verifyNotInvoked(AnalysisListener reportWriter, Constraint constraint) throws AnalysisListenerException {
        verify(reportWriter, never()).beginConstraint(constraint);
        verify(reportWriter, never()).setResult(constraintResult);
        verify(reportWriter, never()).endConstraint();
    }

    private void write(Concept concept, Constraint constraint) throws AnalysisListenerException {
        compositeReportWriter.begin();

        compositeReportWriter.beginGroup(group);

        compositeReportWriter.beginConcept(concept);
        compositeReportWriter.setResult(conceptResult);
        compositeReportWriter.endConcept();

        compositeReportWriter.beginConstraint(constraint);
        compositeReportWriter.setResult(constraintResult);
        compositeReportWriter.endConstraint();

        compositeReportWriter.endGroup();

        compositeReportWriter.end();
    }

    private <T extends ExecutableRule> T getRule(Class<T> type, String... reportTypes) {
        T rule = mock(type);
        Report report = mock(Report.class);
        if (reportTypes.length > 0) {
            Set<String> selection = new HashSet<>(Arrays.asList(reportTypes));
            when(report.getSelectedTypes()).thenReturn(selection);
        } else {
            when(report.getSelectedTypes()).thenReturn(null);
        }

        when(rule.getReport()).thenReturn(report);
        return rule;
    }

}
