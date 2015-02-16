package com.buschmais.jqassistant.plugin.common.test.rule;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Rule;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.common.test.rule.model.TestDescriptor;

/**
 * Verifies rules execution based of scripts.
 */
public class ScriptIT extends AbstractPluginIT {

    @Test
    public void javaScriptConcept() throws AnalysisException {
        applyConcept("javascript:TestConcept");
        verifyResults(reportWriter.getConceptResults(), "javascript:TestConcept", Severity.MINOR);
    }

    @Test
    public void javaScriptConceptUsingGDS() throws AnalysisException {
        applyConcept("javascript:TestConceptUsingGDS");
        verifyResults(reportWriter.getConceptResults(), "javascript:TestConceptUsingGDS", Severity.MINOR);
    }

    @Test
    public void javaScriptConstraint() throws AnalysisException {
        validateConstraint("javascript:TestConstraint");
        verifyResults(reportWriter.getConstraintViolations(), "javascript:TestConstraint", Severity.BLOCKER);
    }

    @Test
    public void groovyConcept() throws AnalysisException {
        applyConcept("groovy:TestConcept");
        verifyResults(reportWriter.getConceptResults(), "groovy:TestConcept", Severity.MINOR);
    }

    @Test
    public void groovyConstraint() throws AnalysisException {
        validateConstraint("groovy:TestConstraint");
        verifyResults(reportWriter.getConstraintViolations(), "groovy:TestConstraint", Severity.BLOCKER);
    }

    @Test
    public void rubyConcept() throws AnalysisException {
        applyConcept("ruby:TestConcept");
        verifyResults(reportWriter.getConceptResults(), "ruby:TestConcept", Severity.MINOR);
    }

    @Test
    public void rubyConstraint() throws AnalysisException {
        validateConstraint("ruby:TestConstraint");
        verifyResults(reportWriter.getConstraintViolations(), "ruby:TestConstraint", Severity.BLOCKER);
    }

    private <R extends Rule> void verifyResults(Map<String, Result<R>> results, String ruleName, Severity severity) {
        store.beginTransaction();
        assertThat("Expecting one analysis result.", results.size(), equalTo(1));
        Result<?> result = results.get(ruleName);
        assertThat("Expecting a result for " + ruleName, result, notNullValue());
        assertThat("Expecting severity " + severity, result.getSeverity(), equalTo(severity));
        List<Map<String, Object>> rows = result.getRows();
        assertThat("Expecting one row for rule " + ruleName, rows.size(), equalTo(1));
        Map<String, Object> row = rows.get(0);
        Object value = row.get("test");
        assertThat("Expecting a column test", value, notNullValue());
        assertThat("Expecting a value of type " + TestDescriptor.class.getName(), value, instanceOf(TestDescriptor.class));
        TestDescriptor testDescriptor = (TestDescriptor) value;
        assertThat("Expecting property with value 'test'", testDescriptor.getName(), equalTo("test"));
        store.commitTransaction();
    }
}
