package com.buschmais.jqassistant.core.rule.api.model;

import com.buschmais.jqassistant.core.rule.api.configuration.Rule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RuleOverridesTest {

    @Mock
    private Rule rule;

    @Test
    void xml() throws Exception {
        verifyRuleset(RuleSetTestHelper.readRuleSet("/rule-overrides.xml", rule));
    }

    /**
    @Test
    void yaml() throws Exception {
        verifyRuleset(RuleSetTestHelper.readRuleSet("/rule-overrides.yaml", rule));
    }
**/
    private void verifyRuleset(RuleSet ruleSet) throws RuleException {
        assertThat(ruleSet.getConceptBucket().isOverridden("test:OverriddenConcept")).isTrue();
        assertThat(ruleSet.getConceptBucket().getOverridingRule(ruleSet.getConceptBucket().getById("test:OverriddenConcept")).getId()).isEqualTo("test:OverridingConcept");
        // TODO: Überprüfen ob bei executedConcepts die richtigen Concepts ausgeführt wurden und das überschriebene Concept nicht ausgeführt wurde

    }

}
