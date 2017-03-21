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
        verifyDefault(ruleSet);
        verifyAggregation(ruleSet);
        verifyRowCount(ruleSet);
    }

    @Test
    public void xml() throws RuleException {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/resultVerification.xml", RULE_CONFIGURATION);
        verifyDefault(ruleSet);
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
