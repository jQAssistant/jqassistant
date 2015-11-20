package com.buschmais.jqassistant.core.analysis.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.api.AnalysisListener;
import com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.RuleException;
import com.buschmais.jqassistant.core.analysis.api.model.ConceptDescriptor;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.CypherExecutable;
import com.buschmais.jqassistant.core.analysis.api.rule.Executable;
import com.buschmais.jqassistant.core.analysis.api.rule.Report;
import com.buschmais.jqassistant.core.analysis.api.rule.RowCountVerification;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSetBuilder;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;
import com.buschmais.jqassistant.core.analysis.api.rule.Verification;
import com.buschmais.jqassistant.core.analysis.api.rule.source.FileRuleSource;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.ResultIterator;
import org.slf4j.Logger;

/**
 * Verifies the functionality of the analyzer visitor.
 */
public class AnalyzerVisitorTest {

    /**
     * Verifies that columns of a query a reported in the order given by the query.
     * 
     * @throws RuleException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void columnOrder() throws RuleException, AnalysisException {
        String statement = "match (n) return n";
        Executable executable = new CypherExecutable(statement);
        Verification verification = new RowCountVerification();
        Report report = new Report("primaryColumn");
        Concept concept = new Concept("test:Concept", "Test Concept", new FileRuleSource(new File("test.xml")), Severity.MINOR, null, executable,
                Collections.<String, Object>emptyMap(), Collections.<String>emptySet(), verification, report);
        RuleSet ruleSet = RuleSetBuilder.newInstance().addConcept(concept).getRuleSet();

        Query.Result.CompositeRowObject row = mock(Query.Result.CompositeRowObject.class);
        List<String> columnNames = Arrays.asList("c0", "c1", "c2", "c3", "c4", "c5", "c6", "c7", "c8", "c9");
        when(row.getColumns()).thenReturn(columnNames);
        ResultIterator<Query.Result.CompositeRowObject> iterator = mock(ResultIterator.class);
        when(iterator.hasNext()).thenReturn(true, false);
        when(iterator.next()).thenReturn(row);
        Query.Result<Query.Result.CompositeRowObject> result = mock(Query.Result.class);
        when(result.iterator()).thenReturn(iterator);
        Store store = mock(Store.class);
        when(store.create(ConceptDescriptor.class)).thenReturn(mock(ConceptDescriptor.class));
        when(store.executeQuery(Mockito.eq(statement), Mockito.anyMap())).thenReturn(result);

        AnalysisListener<AnalysisListenerException> reportWriter = mock(AnalysisListener.class);
        Logger console = mock(Logger.class);

        AnalyzerVisitor analyzerVisitor = new AnalyzerVisitor(ruleSet, store, reportWriter, console);
        analyzerVisitor.visitConcept(concept, Severity.MINOR);

        ArgumentCaptor<Result> resultCaptor = ArgumentCaptor.forClass(Result.class);
        verify(reportWriter).setResult(resultCaptor.capture());
        Result capturedResult = resultCaptor.getValue();
        assertThat("The reported column names must match the given column names.", capturedResult.getColumnNames(), CoreMatchers.<List>equalTo(
                columnNames));
        List<Map<String, Object>> capturedRows = capturedResult.getRows();
        assertThat("Expecting one row.", capturedRows.size(), equalTo(1));
        Map<String, Object> capturedRow = capturedRows.get(0);
        assertThat("The reported column names must match the given column names.", new ArrayList<>(capturedRow.keySet()), equalTo(columnNames));
    }

}
