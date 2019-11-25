package com.buschmais.jqassistant.core.rule.api.model;

import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Verifies reading dependency information by rule set readers
 */
public class RuleDependencyReaderTest {

    @Test
    public void asciidoc() throws RuleException {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/rule-dependencies.adoc", RuleConfiguration.DEFAULT);
        verifyRules(ruleSet);
    }

    @Test
    public void xml() throws RuleException {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/rule-dependencies.xml", RuleConfiguration.DEFAULT);
        verifyRules(ruleSet);
    }

    private void verifyRules(RuleSet ruleSet) throws RuleException {
        // Concepts
        Concept conceptWithOptionalDependency = ruleSet.getConceptBucket().getById("test:ConceptWithOptionalDependency");
        assertThat(conceptWithOptionalDependency, notNullValue());
        assertThat(conceptWithOptionalDependency.getRequiresConcepts().get("test:Concept1"), equalTo(true));
        Concept conceptWithRequiredDependency = ruleSet.getConceptBucket().getById("test:ConceptWithRequiredDependency");
        assertThat(conceptWithRequiredDependency, notNullValue());
        assertThat(conceptWithRequiredDependency.getRequiresConcepts().get("test:Concept1"), equalTo(false));
        Concept conceptWithMixedDependencies = ruleSet.getConceptBucket().getById("test:ConceptWithMixedDependencies");
        assertThat(conceptWithMixedDependencies, notNullValue());
        assertThat(conceptWithMixedDependencies.getRequiresConcepts().get("test:Concept1"), equalTo(true));
        assertThat(conceptWithMixedDependencies.getRequiresConcepts().get("test:Concept2"), equalTo(false));
        assertThat(conceptWithMixedDependencies.getRequiresConcepts().get("test:Concept3"), equalTo(null));
        // Constraints
        Constraint constraintWithOptionalDependency = ruleSet.getConstraintBucket().getById("test:ConstraintWithOptionalDependency");
        assertThat(constraintWithOptionalDependency, notNullValue());
        assertThat(constraintWithOptionalDependency.getRequiresConcepts().get("test:Concept1"), equalTo(true));
        Constraint constraintWithRequiredDependency = ruleSet.getConstraintBucket().getById("test:ConstraintWithRequiredDependency");
        assertThat(constraintWithRequiredDependency, notNullValue());
        assertThat(constraintWithRequiredDependency.getRequiresConcepts().get("test:Concept1"), equalTo(false));
        Constraint constraintWithMixedDependencies = ruleSet.getConstraintBucket().getById("test:ConstraintWithMixedDependencies");
        assertThat(constraintWithMixedDependencies, notNullValue());
        assertThat(constraintWithMixedDependencies.getRequiresConcepts().get("test:Concept1"), equalTo(true));
        assertThat(constraintWithMixedDependencies.getRequiresConcepts().get("test:Concept2"), equalTo(false));
        assertThat(constraintWithMixedDependencies.getRequiresConcepts().get("test:Concept3"), equalTo(null));
    }


}
