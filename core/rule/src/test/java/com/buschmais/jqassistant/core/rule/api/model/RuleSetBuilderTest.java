package com.buschmais.jqassistant.core.rule.api.model;

import java.io.File;
import java.util.Set;

import com.buschmais.jqassistant.core.rule.api.source.FileRuleSource;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;

import com.github.stefanbirkner.systemlambda.SystemLambda;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.rule.api.model.Concept.Activation.IF_AVAILABLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies functionality of the rule set builder.
 */
class RuleSetBuilderTest {

    private static final RuleSource RULE_SOURCE = new FileRuleSource(new File("."), "test.xml");

    @Test
    void duplicateRules() throws RuleException {
        // Concepts

        Concept concept1 = Concept.builder()
            .id("test")
            .ruleSource(RULE_SOURCE)
            .build();
        Concept concept2 = Concept.builder()
            .id("test")
            .ruleSource(RULE_SOURCE)
            .build();
        RuleSetBuilder builder = RuleSetBuilder.newInstance();
        builder.addConcept(concept1);
        assertThatThrownBy(() -> builder.addConcept(concept2)).isInstanceOf(RuleException.class);

        // Constraints
        Constraint constraint1 = Constraint.builder()
            .id("test")
            .ruleSource(RULE_SOURCE)
            .build();
        Constraint constraint2 = Constraint.builder()
            .id("test")
            .ruleSource(RULE_SOURCE)
            .build();
        builder.addConstraint(constraint1);
        assertThatThrownBy(() -> builder.addConstraint(constraint2)).isInstanceOf(RuleException.class);

        // Groups
        Group group1 = Group.builder()
            .id("test")
            .ruleSource(RULE_SOURCE)
            .build();
        Group group2 = Group.builder()
            .id("test")
            .ruleSource(RULE_SOURCE)
            .build();
        builder.addGroup(group1);
        assertThatThrownBy(() -> builder.addGroup(group2)).isInstanceOf(RuleException.class);
    }

    @Test
    void providedConcepts() throws Exception {
        Concept providedConcept = Concept.builder()
            .id("provided")
            .ruleSource(RULE_SOURCE)
            .build();
        Concept.ProvidedConcept resolvableProvidedConcept = Concept.ProvidedConcept.builder()
            .providingConceptId("providing")
            .providedConceptId("provided")
            .activation(IF_AVAILABLE)
            .build();
        Concept providingConcept = Concept.builder()
            .id("providing")
            .providedConcept(resolvableProvidedConcept)
            .ruleSource(RULE_SOURCE)
            .build();
        Concept.ProvidedConcept nonResolvableProvidedConcept = Concept.ProvidedConcept.builder()
            .providingConceptId("non-resolvable-providing")
            .providedConceptId("non-resolvable-provided")
            .activation(IF_AVAILABLE)
            .build();
        Concept nonResolvableProvidingConcept = Concept.builder()
            .id("non-resolvable-providing")
            .providedConcept(nonResolvableProvidedConcept)
            .ruleSource(RULE_SOURCE)
            .build();

        String systemErr = SystemLambda.tapSystemErr(() -> {
            RuleSet ruleSet = RuleSetBuilder.newInstance()
                .addConcept(providedConcept)
                .addConcept(providingConcept)
                .addConcept(nonResolvableProvidingConcept)
                .getRuleSet();
            assertThat(ruleSet.getProvidedConcepts()).hasSize(2)
                .containsEntry("provided", Set.of(resolvableProvidedConcept))
                .containsEntry("non-resolvable-provided", Set.of(nonResolvableProvidedConcept));
            assertThat(ruleSet.getProvidingConceptIds()).hasSize(2)
                .containsEntry("providing", Set.of("provided"))
                .containsEntry("non-resolvable-providing", Set.of("non-resolvable-provided"));
        });

        assertThat(systemErr).hasLineCount(1)
            .contains("WARN")
            .contains("non-resolvable-providing")
            .contains("non-resolvable-provided");
    }
}
