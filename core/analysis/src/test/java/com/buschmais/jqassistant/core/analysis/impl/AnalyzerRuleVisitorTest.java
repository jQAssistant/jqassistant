package com.buschmais.jqassistant.core.analysis.impl;

import java.io.File;
import java.util.*;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.analysis.api.RuleInterpreterPlugin;
import com.buschmais.jqassistant.core.analysis.api.configuration.Analyze;
import com.buschmais.jqassistant.core.analysis.api.model.ConceptDescriptor;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.api.model.Column;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.rule.api.model.*;
import com.buschmais.jqassistant.core.rule.api.reader.RowCountVerification;
import com.buschmais.jqassistant.core.rule.api.source.FileRuleSource;
import com.buschmais.jqassistant.core.shared.transaction.Transactional;
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

import static com.buschmais.jqassistant.core.report.api.ReportHelper.toColumn;
import static com.buschmais.jqassistant.core.report.api.ReportHelper.toRow;
import static com.buschmais.jqassistant.core.rule.api.model.Severity.*;
import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Verifies the functionality of the analyzer visitor.
 */
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class AnalyzerRuleVisitorTest {

    private static final FileRuleSource FILE_RULE_SOURCE = new FileRuleSource(new File("."), "test.xml");
    private static final String PARAMETER_WITHOUT_DEFAULT = "noDefault";
    private static final String PARAMETER_WITH_DEFAULT = "withDefault";
    private static final RowCountVerification ROW_COUNT_VERIFICATION = RowCountVerification.builder()
        .build();

    @Mock
    private Store store;

    @Mock
    private ReportPlugin reportWriter;

    @Mock
    private Analyze configuration;

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
    void setUp() {
        statement = "match (n) return n";
        concept = createConcept(statement);
        constraint = createConstraint(statement);
        columnNames = Arrays.asList("c0", "c1", "c2", "c3", "c4", "c5", "c6", "c7", "c8", "c9");
        ruleParameters = new HashMap<>();
        ruleParameters.put(PARAMETER_WITHOUT_DEFAULT, "value");
        doReturn(ruleParameters).when(configuration)
            .ruleParameters();

        Query.Result<Query.Result.CompositeRowObject> result = createResult(columnNames);
        when(store.executeQuery(eq(statement), anyMap())).thenReturn(result);

        doReturn(store).when(analyzerContext)
            .getStore();
        doAnswer(invocation -> {
            ((Transactional.TransactionalAction<?>) invocation.getArgument(0)).execute();
            return null;
        }).when(store)
            .requireTransaction(any(Transactional.TransactionalAction.class));
        doAnswer(invocation -> ((Transactional.TransactionalSupplier<?, ?>) invocation.getArgument(0)).execute()).when(store)
            .requireTransaction(any(Transactional.TransactionalSupplier.class));

        List<RuleInterpreterPlugin> languagePlugins = new ArrayList<>();
        languagePlugins.add(new CypherRuleInterpreterPlugin());
        ruleInterpreterPlugins.put("cypher", languagePlugins);

        analyzerRuleVisitor = new AnalyzerRuleVisitor(configuration, analyzerContext, ruleInterpreterPlugins, reportWriter);
    }

    /**
     * Verifies that columns of a query a reported in the order given by the query.
     *
     * @throws RuleException
     *     If the test fails.
     */
    @Test
    void columnOrder() throws RuleException {
        doAnswer(i -> {
            Object value = i.getArgument(0);
            return toColumn(value);
        }).when(analyzerContext)
            .toColumn(any());
        doAnswer(i -> {
            ExecutableRule<?> rule = i.getArgument(0);
            Map<String, Column<?>> columns = i.getArgument(1);
            return toRow(rule, columns);
        }).when(analyzerContext)
            .toRow(any(ExecutableRule.class), anyMap());

        analyzerRuleVisitor.visitConcept(concept, Severity.MINOR);

        ArgumentCaptor<Result> resultCaptor = ArgumentCaptor.forClass(Result.class);
        verify(reportWriter).setResult(resultCaptor.capture());
        verify(analyzerContext).toRow(any(ExecutableRule.class), anyMap());
        Result capturedResult = resultCaptor.getValue();
        assertThat("The reported column names must match the given column names.", capturedResult.getColumnNames(), equalTo(columnNames));
        List<Row> capturedRows = capturedResult.getRows();
        assertThat("Expecting one row.", capturedRows.size(), equalTo(1));
        Row capturedRow = capturedRows.get(0);
        assertThat("The reported column names must match the given column names.", new ArrayList<>(capturedRow.getColumns().keySet()), equalTo(columnNames));
    }

    @Test
    void executeConcept() throws RuleException {
        doReturn(Result.Status.SUCCESS).when(analyzerContext)
            .verify(eq(concept), eq(MAJOR), anyList(), anyList());

        Result.Status status = analyzerRuleVisitor.visitConcept(concept, MAJOR);

        assertThat(status, equalTo(Result.Status.SUCCESS));

        ArgumentCaptor<Map<String, Object>> argumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(store).executeQuery(eq(statement), argumentCaptor.capture());
        Map<String, Object> parameters = argumentCaptor.getValue();
        assertThat(parameters.get(PARAMETER_WITHOUT_DEFAULT), equalTo("value"));
        assertThat(parameters.get(PARAMETER_WITH_DEFAULT), equalTo("defaultValue"));

        verifyConceptResult(Result.Status.SUCCESS, MAJOR);
        verify(store).create(ConceptDescriptor.class);
    }

    @Test
    void abstractConcept() throws RuleException {
        Concept abstractConcept = createConcept(null);

        Result.Status status = analyzerRuleVisitor.visitConcept(abstractConcept, MAJOR);

        assertThat(status, equalTo(Result.Status.SUCCESS));
        verify(store, never()).executeQuery(anyString(), anyMap());
        Result<Concept> result = verifyConceptResult(Result.Status.SUCCESS, MAJOR);
        assertThat(result.getColumnNames(), empty());
        assertThat(result.getRows(), empty());
        verify(store).create(ConceptDescriptor.class);
    }

    @Test
    void skipConcept() throws RuleException {
        analyzerRuleVisitor.skipConcept(concept, MAJOR);

        verify(store, never()).executeQuery(eq(statement), anyMap());
        verifyConceptResult(Result.Status.SKIPPED, MAJOR);
        verify(store, never()).create(ConceptDescriptor.class);
    }

    private Result<Concept> verifyConceptResult(Result.Status expectedStatus, Severity expectedSeverity) throws ReportException {
        verify(reportWriter).beginConcept(concept);
        ArgumentCaptor<Result<Concept>> resultCaptor = ArgumentCaptor.forClass(Result.class);
        verify(reportWriter).setResult(resultCaptor.capture());
        Result<Concept> result = resultCaptor.getValue();
        assertThat(result.getStatus(), equalTo(expectedStatus));
        assertThat(result.getSeverity(), equalTo(expectedSeverity));
        verify(reportWriter).endConcept();
        return result;
    }

    @Test
    void executeConstraint() throws RuleException {
        doReturn(Result.Status.FAILURE).when(analyzerContext)
            .verify(eq(constraint), eq(BLOCKER), anyList(), anyList());

        analyzerRuleVisitor.visitConstraint(constraint, BLOCKER);

        ArgumentCaptor<Map> argumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(store).executeQuery(eq(statement), argumentCaptor.capture());
        Map<String, Object> parameters = argumentCaptor.getValue();
        assertThat(parameters.get(PARAMETER_WITHOUT_DEFAULT), equalTo("value"));
        assertThat(parameters.get(PARAMETER_WITH_DEFAULT), equalTo("defaultValue"));
        verifyConstraintResult(Result.Status.FAILURE, BLOCKER);
    }

    @Test
    void abstractConstraint() throws RuleException {
        Constraint abstractConstraint = createConstraint(null);

        analyzerRuleVisitor.visitConstraint(abstractConstraint, BLOCKER);

        Result<?> result = verifyConstraintResult(Result.Status.SUCCESS, BLOCKER);
        assertThat(result.getColumnNames(), empty());
        assertThat(result.getRows(), empty());
    }


    @Test
    void skipConstraint() throws RuleException {
        analyzerRuleVisitor.skipConstraint(constraint, BLOCKER);

        verify(store, never()).executeQuery(eq(statement), anyMap());
        verifyConstraintResult(Result.Status.SKIPPED, BLOCKER);
    }

    private Result<?> verifyConstraintResult(Result.Status expectedStatus, Severity expectedSeverity) throws ReportException {
        verify(reportWriter).beginConstraint(constraint);
        ArgumentCaptor<Result> resultCaptor = ArgumentCaptor.forClass(Result.class);
        verify(reportWriter).setResult(resultCaptor.capture());
        Result<?> result = resultCaptor.getValue();
        assertThat(result.getStatus(), equalTo(expectedStatus));
        assertThat(result.getSeverity(), equalTo(expectedSeverity));
        verify(reportWriter).endConstraint();
        return result;
    }

    @Test
    void skipAppliedConcept() throws RuleException {
        ConceptDescriptor conceptDescriptor = mock(ConceptDescriptor.class);
        doReturn(Result.Status.SUCCESS).when(conceptDescriptor)
            .getStatus();
        when(store.find(ConceptDescriptor.class, concept.getId())).thenReturn(conceptDescriptor);

        assertThat(analyzerRuleVisitor.visitConcept(concept, Severity.MINOR), equalTo(Result.Status.SUCCESS));

        verify(reportWriter, never()).beginConcept(concept);
        verify(reportWriter, never()).endConcept();
        verify(store, never()).create(ConceptDescriptor.class);
        verify(store, never()).executeQuery(eq(statement), anyMap());
    }

    @Test
    void executeAppliedConcept() throws RuleException {
        doReturn(Result.Status.SUCCESS).when(analyzerContext)
            .verify(eq(concept), eq(MINOR), anyList(), anyList());
        ConceptDescriptor conceptDescriptor = mock(ConceptDescriptor.class);
        when(store.find(ConceptDescriptor.class, concept.getId())).thenReturn(conceptDescriptor);
        doReturn(true).when(configuration)
            .executeAppliedConcepts();

        assertThat(analyzerRuleVisitor.visitConcept(concept, Severity.MINOR), equalTo(Result.Status.SUCCESS));

        verify(reportWriter).beginConcept(concept);
        verify(store, never()).create(ConceptDescriptor.class);
    }

    @Test
    void missingParameter() {
        doReturn(emptyMap()).when(configuration)
            .ruleParameters();
        String statement = "match (n) return n";
        Concept concept = createConcept(statement);
        ReportPlugin reportWriter = mock(ReportPlugin.class);
        try {
            AnalyzerRuleVisitor analyzerRuleVisitor = new AnalyzerRuleVisitor(configuration, analyzerContext, ruleInterpreterPlugins, reportWriter);
            analyzerRuleVisitor.visitConcept(concept, Severity.MINOR);
            fail("Expecting an " + RuleException.class.getName());
        } catch (RuleException e) {
            String message = e.getMessage();
            assertThat(message, containsString(concept.getId()));
            assertThat(message, containsString(PARAMETER_WITHOUT_DEFAULT));
        }
    }

    @Test
    void ruleSourceInErrorMessage() {
        String statement = "match (n) return n";
        Concept concept = createConcept(statement);
        when(store.executeQuery(eq(statement), anyMap())).thenThrow(new IllegalStateException("An error"));
        ReportPlugin reportWriter = mock(ReportPlugin.class);
        try {
            AnalyzerRuleVisitor analyzerRuleVisitor = new AnalyzerRuleVisitor(configuration, analyzerContext, ruleInterpreterPlugins, reportWriter);
            analyzerRuleVisitor.visitConcept(concept, Severity.MINOR);
            fail("Expecting a " + RuleException.class.getName());
        } catch (RuleException e) {
            String message = e.getMessage();
            assertThat(message, containsString("test.xml"));
        }
    }

    private Concept createConcept(String statement) {
        Executable<?> executable = statement != null ? new CypherExecutable(statement) : null;
        Parameter parameterWithoutDefaultValue = new Parameter(PARAMETER_WITHOUT_DEFAULT, Parameter.Type.STRING, null);
        Parameter parameterWithDefaultValue = new Parameter(PARAMETER_WITH_DEFAULT, Parameter.Type.STRING, "defaultValue");
        Map<String, Parameter> parameters = new HashMap<>();
        parameters.put(parameterWithoutDefaultValue.getName(), parameterWithoutDefaultValue);
        parameters.put(parameterWithDefaultValue.getName(), parameterWithDefaultValue);
        Report report = Report.builder()
            .primaryColumn("primaryColumn")
            .build();
        return Concept.builder()
            .id("test:Concept")
            .description("Test Concept")
            .ruleSource(FILE_RULE_SOURCE)
            .severity(Severity.MINOR)
            .executable(executable)
            .parameters(parameters)
            .verification(ROW_COUNT_VERIFICATION)
            .report(report)
            .build();
    }

    private Constraint createConstraint(String statement) {
        Executable<?> executable = statement != null ? new CypherExecutable(statement) : null;
        Parameter parameterWithoutDefaultValue = new Parameter(PARAMETER_WITHOUT_DEFAULT, Parameter.Type.STRING, null);
        Parameter parameterWithDefaultValue = new Parameter(PARAMETER_WITH_DEFAULT, Parameter.Type.STRING, "defaultValue");
        Map<String, Parameter> parameters = new HashMap<>();
        parameters.put(parameterWithoutDefaultValue.getName(), parameterWithoutDefaultValue);
        parameters.put(parameterWithDefaultValue.getName(), parameterWithDefaultValue);
        Report report = Report.builder()
            .primaryColumn("primaryColumn")
            .build();
        return Constraint.builder()
            .id("test:Constraint")
            .description("Test Constraint")
            .ruleSource(FILE_RULE_SOURCE)
            .severity(MAJOR)
            .executable(executable)
            .parameters(parameters)
            .verification(ROW_COUNT_VERIFICATION)
            .report(report)
            .build();
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
