package com.buschmais.jqassistant.core.rule.api.model;

import com.buschmais.jqassistant.core.rule.api.configuration.Rule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

/**
 * Verifies reading dependency information by rule set readers
 */
@ExtendWith(MockitoExtension.class)
class RuleDependencyReaderTest {

    @Mock
    private Rule rule;

    @Test
    void asciidoc() throws RuleException {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/rule-dependencies.adoc", rule);
        verifyRules(ruleSet);
    }

    @Test
    void xml() throws RuleException {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/rule-dependencies.xml", rule);
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
        Concept providingConcept = ruleSet.getConceptBucket().getById("test:ProvidingConcept");
        assertThat(providingConcept.getProvidesConcepts(), hasItem("test:Concept1"));
        assertThat(providingConcept.getProvidesConcepts(), hasItem("test:Concept2"));
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
