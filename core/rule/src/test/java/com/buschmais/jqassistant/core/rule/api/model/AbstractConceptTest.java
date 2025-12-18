package com.buschmais.jqassistant.core.rule.api.model;

import com.buschmais.jqassistant.core.rule.api.configuration.Rule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class AbstractConceptTest {

    @Mock
    private Rule rule;

    @Test
    void xml() throws Exception {
        verifyRuleset(RuleSetTestHelper.readRuleSet("/abstract-concept.xml", rule));
    }

    @Test
    void yaml() throws Exception {
        verifyRuleset(RuleSetTestHelper.readRuleSet("/yaml/abstract-concept.yaml", rule));
    }

    private void verifyRuleset(RuleSet ruleSet) throws RuleException {
        assertThat(ruleSet.getConceptBucket()
                .getById("test:AbstractConcept")
                .isAbstract()).isTrue();
        assertThat(ruleSet.getConceptBucket()
                .getById("test:NonAbstractConcept")
                .isAbstract()).isFalse();
    }
}