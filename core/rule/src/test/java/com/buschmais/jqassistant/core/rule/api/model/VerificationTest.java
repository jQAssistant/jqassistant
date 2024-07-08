package com.buschmais.jqassistant.core.rule.api.model;

import com.buschmais.jqassistant.core.rule.api.configuration.Rule;
import com.buschmais.jqassistant.core.rule.api.reader.AggregationVerification;
import com.buschmais.jqassistant.core.rule.api.reader.RowCountVerification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class VerificationTest {

    @Mock
    private Rule rule;

    @Test
    void xml() throws RuleException {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/resultVerification.xml", rule);
        verifyDefault(ruleSet);
        verifyCustomizedDefault(ruleSet);
        verifyAggregation(ruleSet);
        verifyRowCount(ruleSet);
    }

    private void verifyDefault(RuleSet ruleSet) throws RuleException {
        Concept concept = ruleSet.getConceptBucket().getById("test:DefaultVerification");
        Verification verification = concept.getVerification();
        assertThat(verification).isNull();
    }

    private void verifyCustomizedDefault(RuleSet ruleSet) throws RuleException {
        Concept concept = ruleSet.getConceptBucket().getById("test:CustomizedDefaultVerification");
        Verification verification = concept.getVerification();
        assertThat(verification).isInstanceOf(RowCountVerification.class);
        RowCountVerification rowCountVerification = (RowCountVerification) verification;
        assertThat(rowCountVerification.getMin()).isEqualTo(1);
        assertThat(rowCountVerification.getMax()).isEqualTo(2);
    }

    private void verifyAggregation(RuleSet ruleSet) throws RuleException {
        Concept concept = ruleSet.getConceptBucket().getById("test:AggregationVerification");
        Verification verification = concept.getVerification();
        assertThat(verification).isInstanceOf(AggregationVerification.class);
        AggregationVerification aggregationVerification = (AggregationVerification) verification;
        assertThat(aggregationVerification.getMin()).isEqualTo(1);
        assertThat(aggregationVerification.getMax()).isEqualTo(2);
    }

    private void verifyRowCount(RuleSet ruleSet) throws RuleException {
        Concept concept = ruleSet.getConceptBucket().getById("test:RowCountVerification");
        Verification verification = concept.getVerification();
        assertThat(verification).isInstanceOf(RowCountVerification.class);
        RowCountVerification rowCountVerification = (RowCountVerification) verification;
        assertThat(rowCountVerification.getMin()).isEqualTo(1);
        assertThat(rowCountVerification.getMax()).isEqualTo(2);
    }
}
