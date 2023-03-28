package com.buschmais.jqassistant.plugin.common.test.rule;

import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.report.api.model.Column;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.plugin.common.test.rule.model.TestLabelDescriptor;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.FAILURE;
import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Verifies rules execution based of scripts.
 */
public class ScriptIT extends com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT {

    @Test
    public void javaScriptXmlConcept() throws Exception {
        assertThat(applyConcept("javascript:XmlTestConcept").getStatus(), equalTo(SUCCESS));
        verifyResults(reportPlugin.getConceptResults(), "javascript:XmlTestConcept", Severity.MAJOR);
    }

    @Test
    public void javaScriptXmlConstraint() throws Exception {
        assertThat(validateConstraint("javascript:XmlTestConstraint").getStatus(), equalTo(SUCCESS));
        verifyResults(reportPlugin.getConstraintResults(), "javascript:XmlTestConstraint", Severity.BLOCKER);
    }

    @Test
    public void groovyXmlConcept() throws Exception {
        assertThat(applyConcept("groovy:XmlTestConcept").getStatus(), equalTo(SUCCESS));
        verifyResults(reportPlugin.getConceptResults(), "groovy:XmlTestConcept", Severity.MAJOR);
    }

    @Test
    public void groovyXmlConstraint() throws Exception {
        assertThat(validateConstraint("groovy:XmlTestConstraint").getStatus(), equalTo(SUCCESS));
        verifyResults(reportPlugin.getConstraintResults(), "groovy:XmlTestConstraint", Severity.BLOCKER);
    }

    @Test
    public void rubyXmlConcept() throws Exception {
        assertThat(applyConcept("ruby:XmlTestConcept").getStatus(), equalTo(SUCCESS));
        verifyResults(reportPlugin.getConceptResults(), "ruby:XmlTestConcept", Severity.MAJOR);
    }

    @Test
    public void rubyXmlConstraint() throws Exception {
        assertThat(validateConstraint("ruby:XmlTestConstraint").getStatus(), equalTo(FAILURE));
        verifyResults(reportPlugin.getConstraintResults(), "ruby:XmlTestConstraint", Severity.BLOCKER);
    }

    private <R extends ExecutableRule> void verifyResults(Map<String, Result<R>> results, String ruleName, Severity severity) {
        store.beginTransaction();
        assertThat("Expecting one analysis result.", results.size(), equalTo(1));
        Result<?> result = results.get(ruleName);
        assertThat("Expecting a result for " + ruleName, result, notNullValue());
        assertThat("Expecting severity " + severity, result.getSeverity(), equalTo(severity));
        List<Row> rows = result.getRows();
        assertThat("Expecting one row for rule " + ruleName, rows.size(), equalTo(1));
        Map<String, Column<?>> row = rows.get(0).getColumns();
        Column column = row.get("test");
        assertThat("Expecting a column test", column, notNullValue());
        Object value = column.getValue();
        assertThat("Expecting a value of type " + TestLabelDescriptor.class.getName(), value, instanceOf(TestLabelDescriptor.class));
        TestLabelDescriptor testLabelDescriptor = (TestLabelDescriptor) value;
        assertThat("Expecting property with value 'test'", testLabelDescriptor.getName(), equalTo("test"));
        store.commitTransaction();
    }
}
