package com.buschmais.jqassistant.core.analysis.api.rule;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.buschmais.jqassistant.core.rule.api.reader.AggregationVerification;
import com.buschmais.jqassistant.core.rule.api.reader.RowCountVerification;
import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;

public class VerificationTest {

    public static final RuleConfiguration RULE_CONFIGURATION = RuleConfiguration.builder().build();

    @Test
    public void adoc() throws RuleException {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/resultVerification.adoc", RULE_CONFIGURATION);
        verifyRuleSet(ruleSet);
    }

    @Test
    public void xml() throws RuleException {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/resultVerification.xml", RULE_CONFIGURATION);
        verifyRuleSet(ruleSet);
    }

    private void verifyRuleSet(RuleSet ruleSet) throws NoConceptException {
        verifyDefault(ruleSet);
        verifyCustomizedDefault(ruleSet);
        verifyAggregation(ruleSet);
        verifyRowCount(ruleSet);
    }

    private void verifyDefault(RuleSet ruleSet) throws NoConceptException {
        Concept concept = ruleSet.getConceptBucket().getById("test:DefaultVerification");
        Verification verification = concept.getVerification();
        assertThat(verification, instanceOf(RowCountVerification.class));
        RowCountVerification rowCountVerification = (RowCountVerification) verification;
        assertThat(rowCountVerification.getMin(), nullValue());
        assertThat(rowCountVerification.getMax(), nullValue());
    }

    private void verifyCustomizedDefault(RuleSet ruleSet) throws NoConceptException {
        Concept concept = ruleSet.getConceptBucket().getById("test:CustomizedDefaultVerification");
        Verification verification = concept.getVerification();
        assertThat(verification, instanceOf(RowCountVerification.class));
        RowCountVerification rowCountVerification = (RowCountVerification) verification;
        assertThat(rowCountVerification.getMin(), equalTo(1));
        assertThat(rowCountVerification.getMax(), equalTo(2));
    }

    private void verifyAggregation(RuleSet ruleSet) throws NoConceptException {
        Concept concept = ruleSet.getConceptBucket().getById("test:AggregationVerification");
        Verification verification = concept.getVerification();
        assertThat(verification, instanceOf(AggregationVerification.class));
        AggregationVerification aggregationVerification = (AggregationVerification) verification;
        assertThat(aggregationVerification.getMin(), equalTo(1));
        assertThat(aggregationVerification.getMax(), equalTo(2));
    }

    private void verifyRowCount(RuleSet ruleSet) throws NoConceptException {
        Concept concept = ruleSet.getConceptBucket().getById("test:RowCountVerification");
        Verification verification = concept.getVerification();
        assertThat(verification, instanceOf(RowCountVerification.class));
        RowCountVerification rowCountVerification = (RowCountVerification) verification;
        assertThat(rowCountVerification.getMin(), equalTo(1));
        assertThat(rowCountVerification.getMax(), equalTo(2));
    }
}
