package com.buschmais.jqassistant.core.report;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.impl.CompositeReportPlugin;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CompositeReportPluginTest {

    @Mock
    ReportPlugin reportWriter1;

    @Mock
    ReportPlugin reportWriter2;

    @Mock
    private Group group;

    @Mock
    private Result<?> conceptResult;

    @Mock
    private Result<?> constraintResult;

    private CompositeReportPlugin compositeReportPlugin;

    @Before
    public void createReportWriter() {
        Map<String, ReportPlugin> reportWriters = new HashMap<>();
        reportWriters.put("writer1", reportWriter1);
        reportWriters.put("writer2", reportWriter2);
        compositeReportPlugin = new CompositeReportPlugin(reportWriters);
    }

    @Test
    public void noSelection() throws ReportException {
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
    public void selectOneWriter() throws ReportException {
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
    public void selectMultipleWriters() throws ReportException {
        Concept concept = getRule(Concept.class, "writer1", "writer2");
        Constraint constraint = getRule(Constraint.class, "writer1", "writer2");

        write(concept, constraint);

        verifyInvoked(reportWriter1, concept);
        verifyInvoked(reportWriter1, constraint);
        verifyInvoked(reportWriter2, concept);
        verifyInvoked(reportWriter2, constraint);
        verifyGroup();
    }

    private void verifyGroup() throws ReportException {
        for (ReportPlugin reportWriter : Arrays.asList(reportWriter1, reportWriter2)) {
            verify(reportWriter).begin();
            verify(reportWriter).end();
            verify(reportWriter).beginGroup(group);
            verify(reportWriter).endGroup();
        }
    }

    private void verifyInvoked(ReportPlugin reportWriter, Concept concept) throws ReportException {
        verify(reportWriter).beginConcept(concept);
        verify(reportWriter).setResult(conceptResult);
        verify(reportWriter).endConcept();
    }

    private void verifyNotInvoked(ReportPlugin reportWriter, Concept concept) throws ReportException {
        verify(reportWriter, never()).beginConcept(concept);
        verify(reportWriter, never()).setResult(conceptResult);
        verify(reportWriter, never()).endConcept();
    }

    private void verifyInvoked(ReportPlugin reportWriter, Constraint constraint) throws ReportException {
        verify(reportWriter).beginConstraint(constraint);
        verify(reportWriter).setResult(constraintResult);
        verify(reportWriter).endConstraint();
    }

    private void verifyNotInvoked(ReportPlugin reportWriter, Constraint constraint) throws ReportException {
        verify(reportWriter, never()).beginConstraint(constraint);
        verify(reportWriter, never()).setResult(constraintResult);
        verify(reportWriter, never()).endConstraint();
    }

    private void write(Concept concept, Constraint constraint) throws ReportException {
        compositeReportPlugin.begin();

        compositeReportPlugin.beginGroup(group);

        compositeReportPlugin.beginConcept(concept);
        compositeReportPlugin.setResult(conceptResult);
        compositeReportPlugin.endConcept();

        compositeReportPlugin.beginConstraint(constraint);
        compositeReportPlugin.setResult(constraintResult);
        compositeReportPlugin.endConstraint();

        compositeReportPlugin.endGroup();

        compositeReportPlugin.end();
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
