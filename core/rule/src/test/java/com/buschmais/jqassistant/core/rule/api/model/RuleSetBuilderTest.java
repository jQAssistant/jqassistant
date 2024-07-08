package com.buschmais.jqassistant.core.rule.api.model;

import java.io.File;

import com.buschmais.jqassistant.core.rule.api.source.FileRuleSource;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies functionality of the rule set builder.
 */
class RuleSetBuilderTest {

    @Test
    void duplicateRules() throws RuleException {
        RuleSource ruleSource = new FileRuleSource(new File("."), "test.xml");
        // Concepts

        Concept concept1 = Concept.builder()
            .id("test")
            .ruleSource(ruleSource)
            .build();
        Concept concept2 = Concept.builder()
            .id("test")
            .ruleSource(ruleSource)
            .build();
        RuleSetBuilder builder = RuleSetBuilder.newInstance();
        builder.addConcept(concept1);
        assertThatThrownBy(() -> builder.addConcept(concept2)).isInstanceOf(RuleException.class);

        // Constraints
        Constraint constraint1 = Constraint.builder()
            .id("test")
            .ruleSource(ruleSource)
            .build();
        Constraint constraint2 = Constraint.builder()
            .id("test")
            .ruleSource(ruleSource)
            .build();
        builder.addConstraint(constraint1);
        assertThatThrownBy(() -> builder.addConstraint(constraint2)).isInstanceOf(RuleException.class);

        // Groups
        Group group1 = Group.builder()
            .id("test")
            .ruleSource(ruleSource)
            .build();
        Group group2 = Group.builder()
            .id("test")
            .ruleSource(ruleSource)
            .build();
        builder.addGroup(group1);
        assertThatThrownBy(() -> builder.addGroup(group2)).isInstanceOf(RuleException.class);
    }

    @Test
    void providedConcepts() {

    }

}
