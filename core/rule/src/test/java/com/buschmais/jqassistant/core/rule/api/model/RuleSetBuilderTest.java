package com.buschmais.jqassistant.core.rule.api.model;

import java.io.File;
import java.util.Set;

import com.buschmais.jqassistant.core.rule.api.source.FileRuleSource;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

/**
 * Verifies functionality of the rule set builder.
 */
class RuleSetBuilderTest {

    private static final RuleSource RULE_SOURCE = new FileRuleSource(new File("."), "test.xml");

    private static MockedStatic<LoggerFactory> loggerFactory;

    private static Logger logger = Mockito.mock(Logger.class);

    @BeforeAll
    static void setUp() {
        loggerFactory = mockStatic(LoggerFactory.class);
        loggerFactory.when(() -> LoggerFactory.getLogger(RuleSetBuilder.class))
            .thenReturn(logger);
    }

    @AfterAll
    static void tearDown() {
        loggerFactory.close();
    }

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
    void providedConcepts() throws RuleException {
        Concept providedConcept = Concept.builder()
            .id("provided")
            .ruleSource(RULE_SOURCE)
            .build();
        Concept providingConcept = Concept.builder()
            .id("providing")
            .providedConcepts(Set.of("provided"))
            .ruleSource(RULE_SOURCE)
            .build();
        Concept nonResolvableProvidingConcept = Concept.builder()
            .id("non-resolvable-providing")
            .providedConcepts(Set.of("non-resolvable-provided"))
            .ruleSource(RULE_SOURCE)
            .build();

        RuleSet ruleSet = RuleSetBuilder.newInstance()
            .addConcept(providedConcept)
            .addConcept(providingConcept)
            .addConcept(nonResolvableProvidingConcept)
            .getRuleSet();

        assertThat(ruleSet.getProvidedConcepts()).hasSize(2)
            .containsEntry("provided", Set.of("providing"))
            .containsEntry("non-resolvable-provided", Set.of("non-resolvable-providing"));
        assertThat(ruleSet.getProvidingConcepts()).hasSize(2)
            .containsEntry("providing", Set.of("provided"))
            .containsEntry("non-resolvable-providing", Set.of("non-resolvable-provided"));
        ArgumentCaptor<Concept> providingConceptCaptor = ArgumentCaptor.forClass(Concept.class);
        ArgumentCaptor<String> providedConceptCaptor = ArgumentCaptor.forClass(String.class);
        verify(logger).warn(anyString(), providingConceptCaptor.capture(), providedConceptCaptor.capture());
        assertThat(providingConceptCaptor.getValue()
            .getId()).isEqualTo("non-resolvable-providing");
        assertThat(providedConceptCaptor.getValue()).isEqualTo("non-resolvable-provided");
    }
}
