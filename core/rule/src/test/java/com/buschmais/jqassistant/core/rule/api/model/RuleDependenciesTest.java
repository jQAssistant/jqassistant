package com.buschmais.jqassistant.core.rule.api.model;

import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.core.rule.api.configuration.Rule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RuleDependenciesTest {

    @Mock
    private Rule rule;

    @Test
    void xml() throws Exception {
        verifyRuleset(RuleSetTestHelper.readRuleSet("/rule-dependencies.xml", rule));
    }

    @Test
    void yaml() throws Exception {
        verifyRuleset(RuleSetTestHelper.readRuleSet("/rule-dependencies.yaml", rule));
    }

    private void verifyRuleset(RuleSet ruleSet) {
        assertThat(ruleSet.getProvidedConcepts()).containsExactlyEntriesOf(
            Map.of("test:AbstractConcept", Set.of("test:ProvidingConcept1", "test:ProvidingConcept2")));
    }

}
