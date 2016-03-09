package com.buschmais.jqassistant.core.analysis.impl;

import com.buschmais.jqassistant.core.analysis.api.*;
import com.buschmais.jqassistant.core.analysis.api.model.ConceptDescriptor;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.analysis.api.rule.source.FileRuleSource;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.ResultIterator;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import java.io.File;
import java.util.*;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * Verifies the functionality of the analyzer visitor.
 */
@RunWith(MockitoJUnitRunner.class)
public class AnalyzerVisitorTest {

    private static final String RULESOURCE = "test.xml";

    @Mock
    private Store store;

    @Mock
    private Logger console;

    @Mock
    private AnalysisListener<AnalysisListenerException> reportWriter;

    private String statement;

    private Concept concept;

    private RuleSet ruleSet;

    private List<String> columnNames;

    @Before
    public void setUp() throws RuleHandlingException {
        statement = "match (n) return n";
        concept = createConcept(statement);
        ruleSet = RuleSetBuilder.newInstance().addConcept(concept).getRuleSet();
        columnNames = Arrays.asList("c0", "c1", "c2", "c3", "c4", "c5", "c6", "c7", "c8", "c9");

        Query.Result<Query.Result.CompositeRowObject> result = createResult(columnNames);
        when(store.executeQuery(Mockito.eq(statement), Mockito.anyMap())).thenReturn(result);
    }

    /**
     * Verifies that columns of a query a reported in the order given by the query.
     *
     * @throws RuleException     If the test fails.
     * @throws AnalysisException If the test fails.
     */
    @Test
    public void columnOrder() throws RuleException, AnalysisException {

        AnalyzerVisitor analyzerVisitor = new AnalyzerVisitor(ruleSet, false, store, reportWriter, console);
        analyzerVisitor.visitConcept(concept, Severity.MINOR);

        ArgumentCaptor<Result> resultCaptor = ArgumentCaptor.forClass(Result.class);
        verify(reportWriter).setResult(resultCaptor.capture());
        Result capturedResult = resultCaptor.getValue();
        assertThat("The reported column names must match the given column names.", capturedResult.getColumnNames(), CoreMatchers
                .<List>equalTo(columnNames));
        List<Map<String, Object>> capturedRows = capturedResult.getRows();
        assertThat("Expecting one row.", capturedRows.size(), equalTo(1));
        Map<String, Object> capturedRow = capturedRows.get(0);
        assertThat("The reported column names must match the given column names.", new ArrayList<>(capturedRow.keySet()), equalTo(columnNames));
    }

    @Test
    public void executeConcept() throws RuleException, AnalysisException {
        AnalyzerVisitor analyzerVisitor = new AnalyzerVisitor(ruleSet, false, store, reportWriter, console);
        analyzerVisitor.visitConcept(concept, Severity.MINOR);

        verify(reportWriter).beginConcept(concept);
        verify(store).create(ConceptDescriptor.class);
    }

    @Test
    public void skipAppliedConcept() throws RuleException, AnalysisException {
        when(store.find(ConceptDescriptor.class, concept.getId())).thenReturn(mock(ConceptDescriptor.class));

        AnalyzerVisitor analyzerVisitor = new AnalyzerVisitor(ruleSet, false, store, reportWriter, console);
        analyzerVisitor.visitConcept(concept, Severity.MINOR);

        verify(reportWriter, never()).beginConcept(concept);
        verify(store, never()).create(ConceptDescriptor.class);
    }

    @Test
    public void executeAppliedConcept() throws RuleException, AnalysisException {
        when(store.find(ConceptDescriptor.class, concept.getId())).thenReturn(mock(ConceptDescriptor.class));

        AnalyzerVisitor analyzerVisitor = new AnalyzerVisitor(ruleSet, true, store, reportWriter, console);
        analyzerVisitor.visitConcept(concept, Severity.MINOR);

        verify(reportWriter).beginConcept(concept);
        verify(store, never()).create(ConceptDescriptor.class);
    }

    @Test
    public void ruleSourceInErrorMessage() throws RuleException, AnalysisException {
        String statement = "match (n) return n";
        Concept concept = createConcept(statement);
        RuleSet ruleSet = RuleSetBuilder.newInstance().addConcept(concept).getRuleSet();
        when(store.executeQuery(Mockito.eq(statement), Mockito.anyMap())).thenThrow(new IllegalStateException("An error"));
        AnalysisListener<AnalysisListenerException> reportWriter = mock(AnalysisListener.class);
        try {

            AnalyzerVisitor analyzerVisitor = new AnalyzerVisitor(ruleSet, false, store, reportWriter, console);
            analyzerVisitor.visitConcept(concept, Severity.MINOR);
            fail("Expecting an " + AnalysisException.class.getName());
        } catch (AnalysisException e) {
            String message = e.getMessage();
            assertThat(message, containsString(RULESOURCE));
        }
    }

    private Concept createConcept(String statement) {
        Executable executable = new CypherExecutable(statement);
        Verification verification = new RowCountVerification();
        Report report = Report.Builder.newInstance().primaryColumn("primaryColumn").get();
        return Concept.Builder.newConcept().id("test:Concept").description("Test Concept").ruleSource(new FileRuleSource(new File(RULESOURCE))).severity(Severity.MINOR).executable(executable).verification(verification)
                .report(report).get();
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
