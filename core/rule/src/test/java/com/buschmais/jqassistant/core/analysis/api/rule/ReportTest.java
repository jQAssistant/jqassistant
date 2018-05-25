package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Properties;
import java.util.Set;

import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;

/**
 * Verifies reading the report part of rules.
 */
public class ReportTest {

    @Test
    public void reportBuilder() {
        Report emptyReport = Report.Builder.newInstance().get();
        assertThat(emptyReport.getPrimaryColumn(), nullValue());
        assertThat(emptyReport.getSelectedTypes(), nullValue());
        assertThat(emptyReport.getProperties().size(), equalTo(0));
        Report reportWithSelection = Report.Builder.newInstance().selectTypes("a, b").get();
        Set<String> selectedTypes = reportWithSelection.getSelectedTypes();
        assertThat(selectedTypes.size(), equalTo(2));
        assertThat(selectedTypes, hasItems("a", "b"));
        Properties properties = new Properties();
        properties.setProperty("key1", "value1");
        Report reportWithProperties = Report.Builder.newInstance().properties(properties).property("key2", "value2").get();
        Properties reportProperties = reportWithProperties.getProperties();
        assertThat(reportProperties.size(), equalTo(2));
        assertThat(reportProperties.getProperty("key1"), equalTo("value1"));
        assertThat(reportProperties.getProperty("key2"), equalTo("value2"));
        Report reportWithPrimaryColumn = Report.Builder.newInstance().primaryColumn("p").get();
        assertThat(reportWithPrimaryColumn.getPrimaryColumn(), equalTo("p"));
    }

    @Test
    public void asciidocReport() throws Exception {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/report.adoc", RuleConfiguration.DEFAULT);
        verifyReport(ruleSet);
    }

    @Test
    public void xmlReport() throws Exception {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/report.xml", RuleConfiguration.DEFAULT);
        verifyReport(ruleSet);
    }

    private void verifyReport(RuleSet ruleSet) throws NoConceptException, NoConstraintException {
        verifyRule(ruleSet.getConceptBucket().getById("test:Concept"));
        verifyRule(ruleSet.getConstraintBucket().getById("test:Constraint"));
    }

    private void verifyRule(ExecutableRule rule) {
        assertThat("Expecting a rule", rule, notNullValue());
        Report report = rule.getReport();
        Set<String> selection = report.getSelectedTypes();
        assertThat("Expecting a selection of one report.", selection.size(), equalTo(1));
        assertThat("Expecting a custom report type.", selection, hasItem("custom"));
        assertThat("Expecting a primary column.", report.getPrimaryColumn(), equalTo("n"));
        Properties properties = report.getProperties();
        assertThat("Expecting two properties.", properties.size(), equalTo(2));
        assertThat("Expecting value1 for key1.", properties.getProperty("key1"), equalTo("value1"));
        assertThat("Expecting value2 for key2.", properties.getProperty("key2"), equalTo("value2"));
    }

}
