package com.buschmais.jqassistant.core.analysis.impl;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.analysis.api.rule.MetricGroup;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSetBuilder;
import com.buschmais.jqassistant.core.analysis.api.rule.Template;
import com.buschmais.jqassistant.core.analysis.api.rule.source.FileRuleSource;
import com.buschmais.jqassistant.core.analysis.api.rule.source.RuleSource;

/**
 * Verifies functionality of the rule set builder.
 */
public class RuleSetBuilderTest {

    @Test
    public void duplicateRules() throws RuleException {
        RuleSource ruleSource = new FileRuleSource(new File("test.xml"));
        // Concepts
        Concept concept1 = new Concept("test", null,ruleSource, null, null, null, null, null, null, null);
        Concept concept2 = new Concept("test", null, ruleSource, null, null, null, null, null, null, null);
        RuleSetBuilder builder = RuleSetBuilder.newInstance();
        builder.addConcept(concept1);
        try {
            builder.addConcept(concept2);
            Assert.fail("Expecting an exception");
        } catch (RuleException e) {
        }
        // Constraints
        Constraint constraint1 = new Constraint("test", null, ruleSource, null, null, null, null, null, null, null);
        Constraint constraint2 = new Constraint("test", null, ruleSource, null, null, null, null, null, null, null);
        builder.addConstraint(constraint1);
        try {
            builder.addConstraint(constraint2);
            Assert.fail("Expecting an exception");
        } catch (RuleException e) {
        }
        // Groups
        Group group1 = new Group("test", null, ruleSource, null, null, null);
        Group group2 = new Group("test", null, ruleSource, null, null, null);
        builder.addGroup(group1);
        try {
            builder.addGroup(group2);
            Assert.fail("Expecting an exception");
        } catch (RuleException e) {
        }
        // Templates
        Template template1 = new Template("test", null, ruleSource, null, null);
        Template template2 = new Template("test", null, ruleSource, null, null);
        builder.addTemplate(template1);
        try {
            builder.addTemplate(template2);
            Assert.fail("Expecting an exception");
        } catch (RuleException e) {
        }
        // Metric Groups
        MetricGroup metricGroup1 = new MetricGroup("test", null, ruleSource, null);
        MetricGroup metricGroup2 = new MetricGroup("test", null, ruleSource, null);
        builder.addMetricGroup(metricGroup1);
        try {
            builder.addMetricGroup(metricGroup2);
            Assert.fail("Expecting an exception");
        } catch (RuleException e) {
        }
    }

}
