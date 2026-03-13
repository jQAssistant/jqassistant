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
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies rules execution based of scripts.
 */
public class ScriptIT extends com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT {

    @Test
    public void javaScriptXmlConcept() throws Exception {
        assertThat(applyConcept("javascript:XmlTestConcept").getStatus()).isEqualTo(SUCCESS);
        verifyResults(reportPlugin.getConceptResults(), "javascript:XmlTestConcept", Severity.MAJOR);
    }

    @Test
    public void javaScriptXmlConstraint() throws Exception {
        assertThat(validateConstraint("javascript:XmlTestConstraint").getStatus()).isEqualTo(SUCCESS);
        verifyResults(reportPlugin.getConstraintResults(), "javascript:XmlTestConstraint", Severity.BLOCKER);
    }

    @Test
    public void groovyXmlConcept() throws Exception {
        assertThat(applyConcept("groovy:XmlTestConcept").getStatus()).isEqualTo(SUCCESS);
        verifyResults(reportPlugin.getConceptResults(), "groovy:XmlTestConcept", Severity.MAJOR);
    }

    @Test
    public void groovyXmlConstraint() throws Exception {
        assertThat(validateConstraint("groovy:XmlTestConstraint").getStatus()).isEqualTo(SUCCESS);
        verifyResults(reportPlugin.getConstraintResults(), "groovy:XmlTestConstraint", Severity.BLOCKER);
    }

    @Test
    public void rubyXmlConcept() throws Exception {
        assertThat(applyConcept("ruby:XmlTestConcept").getStatus()).isEqualTo(SUCCESS);
        verifyResults(reportPlugin.getConceptResults(), "ruby:XmlTestConcept", Severity.MAJOR);
    }

    @Test
    public void rubyXmlConstraint() throws Exception {
        assertThat(validateConstraint("ruby:XmlTestConstraint").getStatus()).isEqualTo(FAILURE);
        verifyResults(reportPlugin.getConstraintResults(), "ruby:XmlTestConstraint", Severity.BLOCKER);
    }

    private <R extends ExecutableRule> void verifyResults(Map<String, Result<R>> results, String ruleName, Severity severity) {
        store.beginTransaction();
        assertThat(results.size()).as("Expecting one analysis result.").isEqualTo(1);
        Result<?> result = results.get(ruleName);
        assertThat(result).as("Expecting a result for " + ruleName).isNotNull();
        assertThat(result.getSeverity()).as("Expecting severity " + severity).isEqualTo(severity);
        List<Row> rows = result.getRows();
        assertThat(rows.size()).as("Expecting one row for rule " + ruleName).isEqualTo(1);
        Map<String, Column<?>> row = rows.get(0).getColumns();
        Column column = row.get("test");
        assertThat(column).as("Expecting a column test").isNotNull();
        Object value = column.getValue();
        assertThat(value).as("Expecting a value of type " + TestLabelDescriptor.class.getName()).isInstanceOf(TestLabelDescriptor.class);
        TestLabelDescriptor testLabelDescriptor = (TestLabelDescriptor) value;
        assertThat(testLabelDescriptor.getName()).as("Expecting property with value 'test'").isEqualTo("test");
        store.commitTransaction();
    }
}
