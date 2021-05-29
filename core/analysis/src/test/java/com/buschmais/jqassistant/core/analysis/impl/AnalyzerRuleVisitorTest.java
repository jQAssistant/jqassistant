package com.buschmais.jqassistant.core.analysis.impl;

import java.io.File;
import java.util.*;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerConfiguration;
import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.analysis.api.RuleInterpreterPlugin;
import com.buschmais.jqassistant.core.analysis.api.model.ConceptDescriptor;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.*;
import com.buschmais.jqassistant.core.rule.api.reader.RowCountVerification;
import com.buschmais.jqassistant.core.rule.api.source.FileRuleSource;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.ResultIterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.*;

/**
 * Verifies the functionality of the analyzer visitor.
 */
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class AnalyzerRuleVisitorTest {

    private static final String RULESOURCE = "test.xml";
    private static final String PARAMETER_WITHOUT_DEFAULT = "noDefault";
    private static final String PARAMETER_WITH_DEFAULT = "withDefault";
    private static final RowCountVerification ROW_COUNT_VERIFICATION = RowCountVerification.builder().build();

    @Mock
    private Store store;

    @Mock
    private Logger logger;

    @Mock
    private ReportPlugin reportWriter;

    @Mock
    private AnalyzerConfiguration configuration;

    @Mock
    private AnalyzerContext analyzerContext;

    private Map<String, Collection<RuleInterpreterPlugin>> ruleInterpreterPlugins = new HashMap<>();

    private AnalyzerRuleVisitor analyzerRuleVisitor;

    private String statement;

    private Concept concept;

    private Constraint constraint;

    private List<String> columnNames;

    private Map<String, String> ruleParameters;

    @BeforeEach
    public void setUp() {
        statement = "match (n) return n";
        concept = createConcept(statement);
        constraint = createConstraint(statement);
        columnNames = Arrays.asList("c0", "c1", "c2", "c3", "c4", "c5", "c6", "c7", "c8", "c9");
        ruleParameters = new HashMap<>();
        ruleParameters.put(PARAMETER_WITHOUT_DEFAULT, "value");

        Query.Result<Query.Result.CompositeRowObject> result = createResult(columnNames);
        when(store.executeQuery(eq(statement), anyMap())).thenReturn(result);

        doReturn(store).when(analyzerContext).getStore();
        doReturn(logger).when(analyzerContext).getLogger();

        List<RuleInterpreterPlugin> languagePlugins = new ArrayList<>();
        languagePlugins.add(new CypherRuleInterpreterPlugin());
        ruleInterpreterPlugins.put("cypher", languagePlugins);

        analyzerRuleVisitor = new AnalyzerRuleVisitor(configuration, analyzerContext, ruleParameters, ruleInterpreterPlugins, reportWriter);
    }

    /**
     * Verifies that columns of a query a reported in the order given by the query.
     *
     * @throws RuleException
     *             If the test fails.
     */
    @Test
    public void columnOrder() throws RuleException {
        analyzerRuleVisitor.visitConcept(concept, Severity.MINOR);

        ArgumentCaptor<Result> resultCaptor = ArgumentCaptor.forClass(Result.class);
        verify(reportWriter).setResult(resultCaptor.capture());
        Result capturedResult = resultCaptor.getValue();
        assertThat("The reported column names must match the given column names.", capturedResult.getColumnNames(), equalTo(columnNames));
        List<Map<String, Object>> capturedRows = capturedResult.getRows();
        assertThat("Expecting one row.", capturedRows.size(), equalTo(1));
        Map<String, Object> capturedRow = capturedRows.get(0);
        assertThat("The reported column names must match the given column names.", new ArrayList<>(capturedRow.keySet()), equalTo(columnNames));
    }

    @Test
    public void executeConcept() throws RuleException {
        doReturn(Result.Status.SUCCESS).when(analyzerContext).verify(eq(concept), anyList(), anyList());

        boolean visitConcept = analyzerRuleVisitor.visitConcept(concept, Severity.MAJOR);

        assertThat(visitConcept, equalTo(true));

        ArgumentCaptor<Map> argumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(store).executeQuery(eq(statement), argumentCaptor.capture());
        Map<String, Object> parameters = argumentCaptor.getValue();
        assertThat(parameters.get(PARAMETER_WITHOUT_DEFAULT), equalTo("value"));
        assertThat(parameters.get(PARAMETER_WITH_DEFAULT), equalTo("defaultValue"));

        verify(reportWriter).beginConcept(concept);
        ArgumentCaptor<Result> resultCaptor = ArgumentCaptor.forClass(Result.class);
        verify(reportWriter).setResult(resultCaptor.capture());
        Result result = resultCaptor.getValue();
        assertThat(result.getStatus(), equalTo(Result.Status.SUCCESS));
        assertThat(result.getSeverity(), equalTo(Severity.MAJOR));
        verify(reportWriter).endConcept();
        verify(store).create(ConceptDescriptor.class);
    }

    @Test
    public void skipConcept() throws RuleException {
        analyzerRuleVisitor.skipConcept(concept, Severity.MAJOR);

        verify(store, never()).executeQuery(eq(statement), anyMap());

        verify(reportWriter).beginConcept(concept);
        ArgumentCaptor<Result> resultCaptor = ArgumentCaptor.forClass(Result.class);
        verify(reportWriter).setResult(resultCaptor.capture());
        Result result = resultCaptor.getValue();
        assertThat(result.getStatus(), equalTo(Result.Status.SKIPPED));
        assertThat(result.getSeverity(), equalTo(Severity.MAJOR));
        verify(reportWriter).endConcept();
        verify(store, never()).create(ConceptDescriptor.class);
    }

    @Test
    public void executeConstraint() throws RuleException {
        doReturn(Result.Status.FAILURE).when(analyzerContext).verify(eq(constraint), anyList(), anyList());

        analyzerRuleVisitor.visitConstraint(constraint, Severity.BLOCKER);

        ArgumentCaptor<Map> argumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(store).executeQuery(eq(statement), argumentCaptor.capture());
        Map<String, Object> parameters = argumentCaptor.getValue();
        assertThat(parameters.get(PARAMETER_WITHOUT_DEFAULT), equalTo("value"));
        assertThat(parameters.get(PARAMETER_WITH_DEFAULT), equalTo("defaultValue"));

        verify(reportWriter).beginConstraint(constraint);
        ArgumentCaptor<Result> resultCaptor = ArgumentCaptor.forClass(Result.class);
        verify(reportWriter).setResult(resultCaptor.capture());
        Result result = resultCaptor.getValue();
        assertThat(result.getStatus(), equalTo(Result.Status.FAILURE));
        assertThat(result.getSeverity(), equalTo(Severity.BLOCKER));
        verify(reportWriter).endConstraint();
    }

    @Test
    public void skipConstraint() throws RuleException {
        analyzerRuleVisitor.skipConstraint(constraint, Severity.BLOCKER);

        verify(store, never()).executeQuery(eq(statement), anyMap());

        verify(reportWriter).beginConstraint(constraint);
        ArgumentCaptor<Result> resultCaptor = ArgumentCaptor.forClass(Result.class);
        verify(reportWriter).setResult(resultCaptor.capture());
        Result result = resultCaptor.getValue();
        assertThat(result.getStatus(), equalTo(Result.Status.SKIPPED));
        assertThat(result.getSeverity(), equalTo(Severity.BLOCKER));
        verify(reportWriter).endConstraint();
    }

    @Test
    public void skipAppliedConcept() throws RuleException {
        when(store.find(ConceptDescriptor.class, concept.getId())).thenReturn(mock(ConceptDescriptor.class));

        analyzerRuleVisitor.visitConcept(concept, Severity.MINOR);

        verify(reportWriter, never()).beginConcept(concept);
        verify(reportWriter, never()).endConcept();
        verify(store, never()).create(ConceptDescriptor.class);
        verify(store, never()).executeQuery(eq(statement), anyMap());
    }

    @Test
    public void executeAppliedConcept() throws RuleException {
        when(store.find(ConceptDescriptor.class, concept.getId())).thenReturn(mock(ConceptDescriptor.class));
        when(configuration.isExecuteAppliedConcepts()).thenReturn(true);

        analyzerRuleVisitor.visitConcept(concept, Severity.MINOR);

        verify(reportWriter).beginConcept(concept);
        verify(store, never()).create(ConceptDescriptor.class);
    }

    @Test
    public void missingParameter() {
        String statement = "match (n) return n";
        Concept concept = createConcept(statement);
        ReportPlugin reportWriter = mock(ReportPlugin.class);
        try {
            AnalyzerRuleVisitor analyzerRuleVisitor = new AnalyzerRuleVisitor(configuration, analyzerContext, Collections.emptyMap(), ruleInterpreterPlugins,
                    reportWriter);
            analyzerRuleVisitor.visitConcept(concept, Severity.MINOR);
            fail("Expecting an " + RuleException.class.getName());
        } catch (RuleException e) {
            String message = e.getMessage();
            assertThat(message, containsString(concept.getId()));
            assertThat(message, containsString(PARAMETER_WITHOUT_DEFAULT));
        }
    }

    @Test
    public void ruleSourceInErrorMessage() {
        String statement = "match (n) return n";
        Concept concept = createConcept(statement);
        when(store.executeQuery(eq(statement), anyMap())).thenThrow(new IllegalStateException("An error"));
        ReportPlugin reportWriter = mock(ReportPlugin.class);
        try {
            AnalyzerRuleVisitor analyzerRuleVisitor = new AnalyzerRuleVisitor(configuration, analyzerContext, ruleParameters, ruleInterpreterPlugins,
                    reportWriter);
            analyzerRuleVisitor.visitConcept(concept, Severity.MINOR);
            fail("Expecting a " + RuleException.class.getName());
        } catch (RuleException e) {
            String message = e.getMessage();
            assertThat(message, containsString(RULESOURCE));
        }
    }

    private Concept createConcept(String statement) {
        Executable executable = new CypherExecutable(statement);
        Parameter parameterWithoutDefaultValue = new Parameter(PARAMETER_WITHOUT_DEFAULT, Parameter.Type.STRING, null);
        Parameter parameterWithDefaultValue = new Parameter(PARAMETER_WITH_DEFAULT, Parameter.Type.STRING, "defaultValue");
        Map<String, Parameter> parameters = new HashMap<>();
        parameters.put(parameterWithoutDefaultValue.getName(), parameterWithoutDefaultValue);
        parameters.put(parameterWithDefaultValue.getName(), parameterWithDefaultValue);
        Report report = Report.builder().primaryColumn("primaryColumn").build();
        return Concept.builder().id("test:Concept").description("Test Concept").ruleSource(new FileRuleSource(new File(RULESOURCE))).severity(Severity.MINOR)
                .executable(executable).parameters(parameters).verification(ROW_COUNT_VERIFICATION).report(report).build();
    }

    private Constraint createConstraint(String statement) {
        Executable executable = new CypherExecutable(statement);
        Parameter parameterWithoutDefaultValue = new Parameter(PARAMETER_WITHOUT_DEFAULT, Parameter.Type.STRING, null);
        Parameter parameterWithDefaultValue = new Parameter(PARAMETER_WITH_DEFAULT, Parameter.Type.STRING, "defaultValue");
        Map<String, Parameter> parameters = new HashMap<>();
        parameters.put(parameterWithoutDefaultValue.getName(), parameterWithoutDefaultValue);
        parameters.put(parameterWithDefaultValue.getName(), parameterWithDefaultValue);
        Report report = Report.builder().primaryColumn("primaryColumn").build();
        return Constraint.builder().id("test:Constraint").description("Test Constraint").ruleSource(new FileRuleSource(new File(RULESOURCE)))
                .severity(Severity.MAJOR).executable(executable).parameters(parameters).verification(ROW_COUNT_VERIFICATION).report(report).build();
    }

    private Query.Result<Query.Result.CompositeRowObject> createResult(List<String> columnNames) {
        Query.Result.CompositeRowObject row = mock(Query.Result.CompositeRowObject.class);
        when(row.getColumns()).thenReturn(columnNames);
        ResultIterator<Query.Result.CompositeRowObject> iterator = mock(ResultIterator.class);
        when(iterator.hasNext()).thenReturn(true, false);
        when(iterator.next()).thenReturn(row);
        Query.Result<Query.Result.CompositeRowObject> result = mock(Query.Result.class);
        when(result.iterator()).thenReturn(iterator);
        when(store.create(ConceptDescriptor.class)).thenReturn(mock(ConceptDescriptor.class));
        return result;
    }
}
