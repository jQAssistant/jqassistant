package com.buschmais.jqassistant.core.analysis.api.rule;

import java.io.File;

import com.buschmais.jqassistant.core.rule.api.source.FileRuleSource;
import org.junit.Assert;
import org.junit.Test;

import com.buschmais.jqassistant.core.rule.api.source.RuleSource;

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
    }

}
