package com.buschmais.jqassistant.core.rule.api.model;

import java.util.Properties;
import java.util.Set;

import com.buschmais.jqassistant.core.rule.api.configuration.Rule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

/**
 * Verifies reading the report part of rules.
 */
@ExtendWith(MockitoExtension.class)
class ReportTest {

    @Mock
    private Rule rule;

    @Test
    void reportBuilder() {
        Report emptyReport = Report.builder().build();
        assertThat(emptyReport.getPrimaryColumn()).isNull();
        assertThat(emptyReport.getSelectedTypes()).isNull();
        assertThat(emptyReport.getProperties().size()).isEqualTo(0);
        Report reportWithSelection = Report.builder().selectedTypes(Report.selectTypes("a, b")).build();
        Set<String> selectedTypes = reportWithSelection.getSelectedTypes();
        assertThat(selectedTypes.size()).isEqualTo(2);
        assertThat(selectedTypes, hasItems("a", "b"));
        Properties properties = new Properties();
        properties.setProperty("key1", "value1");
        properties.setProperty("key2", "value2");
        Report reportWithProperties = Report.builder().properties(properties).build();
        Properties reportProperties = reportWithProperties.getProperties();
        assertThat(reportProperties.size()).isEqualTo(2);
        assertThat(reportProperties.getProperty("key1")).isEqualTo("value1");
        assertThat(reportProperties.getProperty("key2")).isEqualTo("value2");
        Report reportWithPrimaryColumn = Report.builder().primaryColumn("p").build();
        assertThat(reportWithPrimaryColumn.getPrimaryColumn()).isEqualTo("p");
    }

    @Test
    void xmlReport() throws Exception {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/report.xml", rule);
        verifyRule(ruleSet.getConceptBucket().getById("test:Concept"));
        verifyRule(ruleSet.getConstraintBucket().getById("test:Constraint"));
    }

    private void verifyRule(ExecutableRule rule) {
        assertThat(rule).as("Expecting a rule").isNotNull();
        Report report = rule.getReport();
        Set<String> selection = report.getSelectedTypes();
        assertThat(selection.size()).as("Expecting a selection of one report.").isEqualTo(1);
        assertThat("Expecting a custom report type.", selection, hasItem("custom"));
        assertThat(report.getPrimaryColumn()).as("Expecting a primary column.").isEqualTo("n");
        Properties properties = report.getProperties();
        assertThat(properties.size()).as("Expecting two properties.").isEqualTo(2);
        assertThat(properties.getProperty("key1")).as("Expecting value1 for key1.").isEqualTo("value1");
        assertThat(properties.getProperty("key2")).as("Expecting value2 for key2.").isEqualTo("value2");
    }

}
