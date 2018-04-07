package com.buschmais.jqassistant.core.analysis.api.rule;

import java.io.File;

import com.buschmais.jqassistant.core.rule.api.source.FileRuleSource;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;

import org.junit.Assert;
import org.junit.Test;

/**
 * Verifies functionality of the rule set builder.
 */
public class RuleSetBuilderTest {

    @Test
    public void duplicateRules() throws RuleException {
        RuleSource ruleSource = new FileRuleSource(new File("test.xml"));
        // Concepts

        Concept concept1 = Concept.builder().id("test").ruleSource(ruleSource).build();
        Concept concept2 = Concept.builder().id("test").ruleSource(ruleSource).build();
        RuleSetBuilder builder = RuleSetBuilder.newInstance();
        builder.addConcept(concept1);
        try {
            builder.addConcept(concept2);
            Assert.fail("Expecting an exception");
        } catch (RuleException e) {
        }
        // Constraints
        Constraint constraint1 = Constraint.builder().id("test").ruleSource(ruleSource).build();
        Constraint constraint2 = Constraint.builder().id("test").ruleSource(ruleSource).build();
        builder.addConstraint(constraint1);
        try {
            builder.addConstraint(constraint2);
            Assert.fail("Expecting an exception");
        } catch (RuleException e) {
        }
        // Groups
        Group group1 = Group.builder().id("test").ruleSource(ruleSource).build();
        Group group2 = Group.builder().id("test").ruleSource(ruleSource).build();
        builder.addGroup(group1);
        try {
            builder.addGroup(group2);
            Assert.fail("Expecting an exception");
        } catch (RuleException e) {
        }
    }

}
