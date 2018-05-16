package com.buschmais.jqassistant.plugin.common.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.impl.ReportContextImpl;
import com.buschmais.jqassistant.plugin.common.impl.report.JUnitReportPlugin;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.FAILURE;
import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class JUnitReportPluginTest {

    private JUnitReportPlugin plugin = new JUnitReportPlugin();

    private ReportContext reportContext;

    private Group testGroup = Group.builder().id("test:Group").build();
    private Concept concept = Concept.builder().id("test:concept").severity(Severity.MINOR).build();
    private Concept majorConcept = Concept.builder().id("test:majorConcept").severity(Severity.MINOR).build();
    private Concept criticalConcept = Concept.builder().id("test:criticalConcept").severity(Severity.CRITICAL).build();
    private Constraint constraint = Constraint.builder().id("test:constraint").severity(Severity.MAJOR).build();
    private Constraint majorConstraint = Constraint.builder().id("test:majorConstraint").severity(Severity.MAJOR).build();
    private Constraint criticalConstraint = Constraint.builder().id("test:criticalConstraint").severity(Severity.CRITICAL).build();

    @Before
    public void setUp() throws ReportException {
        plugin.initialize();
        File outputDirectory = new File("target/test");
        reportContext = new ReportContextImpl(outputDirectory);
    }

    @Test
    public void junitReport() throws ReportException {
        Map<String, Object> properties = new HashMap<>();
        properties.put(JUnitReportPlugin.JUNIT_ERROR_SEVERITY, Severity.CRITICAL.name());

        plugin.configure(reportContext, properties);
        plugin.begin();
        process(concept, SUCCESS);
        process(constraint, SUCCESS);
        plugin.beginGroup(testGroup);
        process(majorConcept, FAILURE);
        process(criticalConcept, FAILURE);
        process(majorConstraint, FAILURE);
        process(criticalConstraint, FAILURE);
        plugin.endGroup();
        plugin.end();

        File reportDirectory = new File(reportContext.getOutputDirectory(), "report");
        assertThat(reportDirectory.exists(), equalTo(true));
        File junitReportDirectory = new File(reportDirectory, "junit");
        assertThat(junitReportDirectory.exists(), equalTo(true));
        File jQAssistantReport = new File(junitReportDirectory, "TEST-jQAssistant.xml");
        assertThat(jQAssistantReport.exists(), equalTo(true));
    }

    private void process(Constraint constraint, Result.Status status) throws ReportException {
        plugin.beginConstraint(constraint);
        plugin.setResult(this.<ExecutableRule> createResult(constraint, status));
        plugin.endConstraint();
    }

    private void process(Concept concept, Result.Status status) throws ReportException {
        plugin.beginConcept(concept);
        plugin.setResult(this.<ExecutableRule> createResult(concept, status));
        plugin.endConcept();
    }

    private <T extends ExecutableRule<?>> Result<T> createResult(T rule, Result.Status status) {
        HashMap<String, Object> row = new HashMap<>();
        row.put("c", "foo");
        return Result.<T> builder().rule(rule).severity(rule.getSeverity()).status(status).columnNames(asList("c")).rows(asList(row)).build();
    }

}
