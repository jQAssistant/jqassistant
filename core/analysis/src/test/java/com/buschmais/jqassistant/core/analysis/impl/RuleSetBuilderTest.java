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

        Concept concept1 = Concept.Builder.newConcept().id("test").ruleSource(ruleSource).get();
        Concept concept2 = Concept.Builder.newConcept().id("test").ruleSource(ruleSource).get();
        RuleSetBuilder builder = RuleSetBuilder.newInstance();
        builder.addConcept(concept1);
        try {
            builder.addConcept(concept2);
            Assert.fail("Expecting an exception");
        } catch (RuleException e) {
        }
        // Constraints
        Constraint constraint1 = Constraint.Builder.newConstraint().id("test").ruleSource(ruleSource).get();
        Constraint constraint2 = Constraint.Builder.newConstraint().id("test").ruleSource(ruleSource).get();
        builder.addConstraint(constraint1);
        try {
            builder.addConstraint(constraint2);
            Assert.fail("Expecting an exception");
        } catch (RuleException e) {
        }
        // Groups
        Group group1 = Group.Builder.newGroup().id("test").ruleSource(ruleSource).get();
        Group group2 = Group.Builder.newGroup().id("test").ruleSource(ruleSource).get();
        builder.addGroup(group1);
        try {
            builder.addGroup(group2);
            Assert.fail("Expecting an exception");
        } catch (RuleException e) {
        }
        // Templates
        Template template1 = Template.Builder.newTemplate().id("test").ruleSource(ruleSource).get();
        Template template2 = Template.Builder.newTemplate().id("test").ruleSource(ruleSource).get();
        builder.addTemplate(template1);
        try {
            builder.addTemplate(template2);
            Assert.fail("Expecting an exception");
        } catch (RuleException e) {
        }
        // Metric Groups
        MetricGroup metricGroup1 = MetricGroup.Builder.newMetricGroup().id("test").ruleSource(ruleSource).get();
        MetricGroup metricGroup2 = MetricGroup.Builder.newMetricGroup().id("test").ruleSource(ruleSource).get();
        builder.addMetricGroup(metricGroup1);
        try {
            builder.addMetricGroup(metricGroup2);
            Assert.fail("Expecting an exception");
        } catch (RuleException e) {
        }
    }

}
