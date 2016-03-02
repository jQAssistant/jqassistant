package com.buschmais.jqassistant.core.analysis.impl;

import com.buschmais.jqassistant.core.analysis.api.rule.*;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Verifies reading the report part of rules.
 */
public class ReportTest {

    @Test
    public void asciidocReport() throws Exception {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/report.adoc");
        verifyReport(ruleSet);
    }

    @Test
    public void xmlReport() throws Exception {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/report.xml");
        verifyReport(ruleSet);
    }

    private void verifyReport(RuleSet ruleSet) throws NoConceptException, NoConstraintException {
        verifyRule(ruleSet.getConceptBucket().getById("test:Concept"));
        verifyRule(ruleSet.getConstraintBucket().getById("test:Constraint"));
    }

    private void verifyRule(ExecutableRule rule) {
        assertThat("Expecting a rule", rule, notNullValue());
        Report report = rule.getReport();
        assertThat("Expecting a custom report type.", report.getType(), equalTo("custom"));
        assertThat("Expecting a primary column.", report.getPrimaryColumn(), equalTo("n"));
        Properties properties = report.getProperties();
        assertThat("Expecting two properties.", properties.size(), equalTo(2));
        assertThat("Expecting value1 for key1.", properties.getProperty("key1"), equalTo("value1"));
        assertThat("Expecting value2 for key2.", properties.getProperty("key2"), equalTo("value2"));
    }

}
