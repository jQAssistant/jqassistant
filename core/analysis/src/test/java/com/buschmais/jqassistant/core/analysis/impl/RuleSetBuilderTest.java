package com.buschmais.jqassistant.core.analysis.impl;

import org.junit.Assert;
import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.*;

/**
 * Verifies functionality of the rule set builder.
 */
public class RuleSetBuilderTest {

    @Test
    public void duplicateRules() throws RuleException {
        // Concepts
        Concept concept1 = new Concept("test", null, null, null, null, null, null, null, null, null);
        Concept concept2 = new Concept("test", null, null, null, null, null, null, null, null, null);
        RuleSetBuilder builder = RuleSetBuilder.newInstance();
        builder.addConcept(concept1);
        try {
            builder.addConcept(concept2);
            Assert.fail("Expecting an exception");
        } catch (RuleException e) {
        }
        // Constraints
        Constraint constraint1 = new Constraint("test", null, null, null, null, null, null, null, null, null);
        Constraint constraint2 = new Constraint("test", null, null, null, null, null, null, null, null, null);
        builder.addConstraint(constraint1);
        try {
            builder.addConstraint(constraint2);
            Assert.fail("Expecting an exception");
        } catch (RuleException e) {
        }
        // Groups
        Group group1 = new Group("test", null, null, null, null);
        Group group2 = new Group("test", null, null, null, null);
        builder.addGroup(group1);
        try {
            builder.addGroup(group2);
            Assert.fail("Expecting an exception");
        } catch (RuleException e) {
        }
        // Templates
        Template template1 = new Template("test", null, null, null);
        Template template2 = new Template("test", null, null, null);
        builder.addTemplate(template1);
        try {
            builder.addTemplate(template2);
            Assert.fail("Expecting an exception");
        } catch (RuleException e) {
        }
        // Metric Groups
        MetricGroup metricGroup1 = new MetricGroup("test", null, null);
        MetricGroup metricGroup2 = new MetricGroup("test", null, null);
        builder.addMetricGroup(metricGroup1);
        try {
            builder.addMetricGroup(metricGroup2);
            Assert.fail("Expecting an exception");
        } catch (RuleException e) {
        }
    }

}
