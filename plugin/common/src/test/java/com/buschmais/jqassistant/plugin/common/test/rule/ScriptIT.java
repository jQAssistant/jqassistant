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
    public void javaScriptXmlConcept() throws AnalysisException {
        assertThat(applyConcept("javascript:XmlTestConcept").getStatus(), equalTo(Result.Status.SUCCESS));
        verifyResults(reportWriter.getConceptResults(), "javascript:XmlTestConcept", Severity.MAJOR);
    }

    @Test
    public void JavaScriptAsciiDocConcept() throws AnalysisException {
        assertThat(applyConcept("javascript:AsciiDocTestConcept").getStatus(), equalTo(Result.Status.SUCCESS));
        verifyResults(reportWriter.getConceptResults(), "javascript:AsciiDocTestConcept", Severity.MAJOR);
    }

    @Test
    public void javaScriptXmlConceptUsingGDS() throws AnalysisException {
        assertThat(applyConcept("javascript:XmlTestConceptUsingGDS").getStatus(), equalTo(Result.Status.SUCCESS));
        verifyResults(reportWriter.getConceptResults(), "javascript:XmlTestConceptUsingGDS", Severity.MAJOR);
    }

    @Test
    public void javaScriptXmlConstraint() throws AnalysisException {
        validateConstraint("javascript:XmlTestConstraint");
        verifyResults(reportWriter.getConstraintResults(), "javascript:XmlTestConstraint", Severity.BLOCKER);
    }

    @Test
    public void JavaScriptAsciiDocConstraint() throws AnalysisException {
        validateConstraint("javascript:AsciiDocTestConstraint");
        verifyResults(reportWriter.getConstraintResults(), "javascript:AsciiDocTestConstraint", Severity.BLOCKER);
    }

    @Test
    public void groovyXmlConcept() throws AnalysisException {
        assertThat(applyConcept("groovy:XmlTestConcept").getStatus(), equalTo(Result.Status.SUCCESS));
        verifyResults(reportWriter.getConceptResults(), "groovy:XmlTestConcept", Severity.MAJOR);
    }

    @Test
    public void groovyXmlConstraint() throws AnalysisException {
        validateConstraint("groovy:XmlTestConstraint");
        verifyResults(reportWriter.getConstraintResults(), "groovy:XmlTestConstraint", Severity.BLOCKER);
    }

    @Test
    public void rubyXmlConcept() throws AnalysisException {
        assertThat(applyConcept("ruby:XmlTestConcept").getStatus(), equalTo(Result.Status.SUCCESS));
        verifyResults(reportWriter.getConceptResults(), "ruby:XmlTestConcept", Severity.MAJOR);
    }

    @Test
    public void rubyXmlConstraint() throws AnalysisException {
        validateConstraint("ruby:XmlTestConstraint");
        verifyResults(reportWriter.getConstraintResults(), "ruby:XmlTestConstraint", Severity.BLOCKER);
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
